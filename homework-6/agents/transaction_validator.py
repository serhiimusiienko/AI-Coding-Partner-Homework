from __future__ import annotations

import argparse
import json
import logging
import uuid
from copy import deepcopy
from dataclasses import dataclass
from datetime import datetime, timezone
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any

LOG_FORMAT: str = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"
LOGGER_NAME: str = "transaction_validator"
DEFAULT_SAMPLE_PATH: Path = Path(__file__).resolve().parents[1] / "sample-transactions.json"


@dataclass(frozen=True)
class ValidationResult:
    transaction_id: str
    status: str
    reason: str
    amount: str
    currency: str


class TransactionValidator:
    REQUIRED_FIELDS: tuple[str, ...] = (
        "transaction_id",
        "amount",
        "currency",
        "source_account",
        "destination_account",
    )
    ALLOWED_CURRENCIES: frozenset[str] = frozenset({"USD", "EUR", "GBP", "JPY", "CHF"})

    def __init__(self, logger: logging.Logger | None = None) -> None:
        self.logger: logging.Logger = logger or logging.getLogger(LOGGER_NAME)

    def process_message(self, message: dict[str, Any]) -> dict[str, Any]:
        message_copy: dict[str, Any] = deepcopy(message)
        data: dict[str, Any] = self._ensure_data_dict(message_copy)

        transaction_id: str = str(data.get("transaction_id", "UNKNOWN"))
        source_account: str = str(data.get("source_account", ""))
        destination_account: str = str(data.get("destination_account", ""))

        status: str = "validated"
        reason: str | None = None

        try:
            missing_field: str | None = self._find_missing_field(data)
            if missing_field is not None:
                status = "rejected"
                reason = "MISSING_FIELD"
            elif not self._is_amount_valid(data["amount"]):
                status = "rejected"
                reason = "INVALID_AMOUNT"
            elif not self._is_currency_valid(data["currency"]):
                status = "rejected"
                reason = "INVALID_CURRENCY"
        except Exception:
            status = "rejected"
            reason = "VALIDATION_ERROR"
            self.logger.exception("Unexpected validation failure for transaction %s", transaction_id)

        data["status"] = status
        if reason is None:
            data.pop("reason", None)
        else:
            data["reason"] = reason

        self._log_audit(transaction_id, status, source_account, destination_account)
        return message_copy

    def _ensure_data_dict(self, message: dict[str, Any]) -> dict[str, Any]:
        data: Any = message.get("data")
        if not isinstance(data, dict):
            data = {}
            message["data"] = data
        return data

    def _find_missing_field(self, data: dict[str, Any]) -> str | None:
        for field in self.REQUIRED_FIELDS:
            value: Any = data.get(field)
            if value is None:
                return field
            if isinstance(value, str) and value.strip() == "":
                return field
        return None

    def _is_amount_valid(self, amount_value: Any) -> bool:
        try:
            amount: Decimal = Decimal(str(amount_value))
        except (InvalidOperation, ValueError):
            return False
        return amount > Decimal("0")

    def _is_currency_valid(self, currency_value: Any) -> bool:
        return str(currency_value).upper() in self.ALLOWED_CURRENCIES

    def _log_audit(
        self,
        transaction_id: str,
        status: str,
        source_account: str,
        destination_account: str,
    ) -> None:
        masked_source: str = self._mask_account(source_account)
        masked_destination: str = self._mask_account(destination_account)
        # Keep an explicit ISO 8601 timestamp in the message body for audit traceability.
        audit_timestamp: str = datetime.now(timezone.utc).isoformat()
        self.logger.info(
            "timestamp=%s txn_id=%s outcome=%s source=%s destination=%s",
            audit_timestamp,
            transaction_id,
            status,
            masked_source,
            masked_destination,
        )

    def _mask_account(self, account_number: str) -> str:
        if account_number == "":
            return "****"
        return f"****{account_number[-4:]}"


def process_message(message: dict[str, Any]) -> dict[str, Any]:
    validator: TransactionValidator = TransactionValidator()
    return validator.process_message(message)


def _configure_logging() -> None:
    root_logger: logging.Logger = logging.getLogger()
    if not root_logger.handlers:
        logging.basicConfig(
            level=logging.INFO,
            format=LOG_FORMAT,
            datefmt="%Y-%m-%dT%H:%M:%S%z",
        )


def _build_message_from_transaction(transaction: dict[str, Any]) -> dict[str, Any]:
    return {
        "message_id": str(uuid.uuid4()),
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "source_agent": "integrator",
        "target_agent": LOGGER_NAME,
        "message_type": "transaction",
        "data": deepcopy(transaction),
    }


def _to_result_row(message_result: dict[str, Any]) -> ValidationResult:
    data: dict[str, Any] = message_result.get("data", {})
    return ValidationResult(
        transaction_id=str(data.get("transaction_id", "UNKNOWN")),
        status=str(data.get("status", "unknown")),
        reason=str(data.get("reason", "")),
        amount=str(data.get("amount", "")),
        currency=str(data.get("currency", "")),
    )


def _print_results_table(results: list[ValidationResult]) -> None:
    headers: list[str] = ["transaction_id", "status", "reason", "amount", "currency"]
    rows: list[list[str]] = [
        [result.transaction_id, result.status, result.reason or "-", result.amount, result.currency]
        for result in results
    ]

    widths: list[int] = [len(header) for header in headers]
    for row in rows:
        widths = [max(current, len(cell)) for current, cell in zip(widths, row)]

    separator: str = "+-" + "-+-".join("-" * width for width in widths) + "-+"

    def format_row(cells: list[str]) -> str:
        padded_cells: list[str] = [cell.ljust(width) for cell, width in zip(cells, widths)]
        return "| " + " | ".join(padded_cells) + " |"

    print(separator)
    print(format_row(headers))
    print(separator)
    for row in rows:
        print(format_row(row))
    print(separator)


def _run_dry_run(input_path: Path) -> int:
    try:
        with input_path.open("r", encoding="utf-8") as file_handle:
            transactions: Any = json.load(file_handle)
    except FileNotFoundError:
        print(f"Input file not found: {input_path}")
        return 1
    except json.JSONDecodeError as exc:
        print(f"Invalid JSON in input file {input_path}: {exc}")
        return 1

    if not isinstance(transactions, list):
        print(f"Expected a list of transactions in {input_path}")
        return 1

    validator: TransactionValidator = TransactionValidator()
    results: list[ValidationResult] = []
    for raw_transaction in transactions:
        if not isinstance(raw_transaction, dict):
            continue
        message: dict[str, Any] = _build_message_from_transaction(raw_transaction)
        validated_message: dict[str, Any] = validator.process_message(message)
        results.append(_to_result_row(validated_message))

    _print_results_table(results)

    total_count: int = len(results)
    valid_count: int = sum(1 for item in results if item.status == "validated")
    invalid_count: int = total_count - valid_count
    print(f"\nSummary: total={total_count} validated={valid_count} rejected={invalid_count}")
    return 0


def _parse_args() -> argparse.Namespace:
    parser: argparse.ArgumentParser = argparse.ArgumentParser(
        description="Transaction Validator",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate sample transactions and print results as a table",
    )
    parser.add_argument(
        "--input",
        default=str(DEFAULT_SAMPLE_PATH),
        help="Path to the transactions JSON file used by --dry-run",
    )
    return parser.parse_args()


def main() -> int:
    _configure_logging()
    args: argparse.Namespace = _parse_args()

    if args.dry_run:
        return _run_dry_run(Path(args.input))

    print("Nothing to do. Use --dry-run to validate sample transactions.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
