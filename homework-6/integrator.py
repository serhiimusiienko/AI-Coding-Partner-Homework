from __future__ import annotations

import json
import logging
import uuid
from copy import deepcopy
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from agents.fraud_detector import process_message as fraud_process_message
from agents.settlement_processor import process_message as settlement_process_message
from agents.transaction_validator import process_message as validator_process_message

LOG_FORMAT: str = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"
LOGGER_NAME: str = "integrator"
BASE_DIR: Path = Path(__file__).resolve().parent
SAMPLE_TRANSACTIONS_PATH: Path = BASE_DIR / "sample-transactions.json"
SHARED_DIR: Path = BASE_DIR / "shared"
INPUT_DIR: Path = SHARED_DIR / "input"
PROCESSING_DIR: Path = SHARED_DIR / "processing"
OUTPUT_DIR: Path = SHARED_DIR / "output"
RESULTS_DIR: Path = SHARED_DIR / "results"


def _configure_logging() -> None:
    root_logger: logging.Logger = logging.getLogger()
    if not root_logger.handlers:
        logging.basicConfig(level=logging.INFO, format=LOG_FORMAT, datefmt="%Y-%m-%dT%H:%M:%S%z")


def _mask_account(account_number: str) -> str:
    if account_number == "":
        return "****"
    return f"****{account_number[-4:]}"


def _ensure_shared_directories() -> None:
    for directory in (INPUT_DIR, PROCESSING_DIR, OUTPUT_DIR, RESULTS_DIR):
        directory.mkdir(parents=True, exist_ok=True)


def _load_transactions(input_path: Path) -> list[dict[str, Any]]:
    with input_path.open("r", encoding="utf-8") as file_handle:
        loaded: Any = json.load(file_handle)

    if not isinstance(loaded, list):
        raise ValueError(f"Expected a list of transactions in {input_path}")

    transactions: list[dict[str, Any]] = []
    for item in loaded:
        if isinstance(item, dict):
            transactions.append(item)
    return transactions


def _build_message(transaction: dict[str, Any]) -> dict[str, Any]:
    return {
        "message_id": str(uuid.uuid4()),
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "source_agent": "integrator",
        "target_agent": "transaction_validator",
        "message_type": "transaction",
        "data": deepcopy(transaction),
    }


def _write_json(path: Path, payload: dict[str, Any]) -> None:
    with path.open("w", encoding="utf-8") as file_handle:
        json.dump(payload, file_handle, indent=2)


def _run_stage(
    message: dict[str, Any],
    stage_name: str,
    source_agent: str,
    target_agent: str,
    stage_function: Any,
) -> dict[str, Any]:
    transaction_id: str = str(message.get("data", {}).get("transaction_id", "UNKNOWN"))

    staged_message: dict[str, Any] = deepcopy(message)
    staged_message["source_agent"] = source_agent
    staged_message["target_agent"] = target_agent
    staged_message["timestamp"] = datetime.now(timezone.utc).isoformat()

    _write_json(PROCESSING_DIR / f"{transaction_id}_{stage_name}.json", staged_message)

    processed_message: dict[str, Any] = stage_function(staged_message)

    _write_json(OUTPUT_DIR / f"{transaction_id}_{stage_name}.json", processed_message)
    return processed_message


def _classify_result(message: dict[str, Any]) -> str:
    data: dict[str, Any] = message.get("data", {})
    status: str = str(data.get("status", "")).lower()
    settlement_status: str = str(data.get("settlement_status", "")).upper()

    if status == "rejected":
        return "rejected"

    if settlement_status == "PENDING_REVIEW" or status in {"held", "pending_review"}:
        return "pending_review"

    if settlement_status == "SETTLED" or status == "settled":
        return "settled"

    return "pending_review"


def _log_pipeline_step(logger: logging.Logger, message: dict[str, Any]) -> None:
    data: dict[str, Any] = message.get("data", {})
    transaction_id: str = str(data.get("transaction_id", "UNKNOWN"))
    status: str = str(data.get("status", "unknown"))
    source_account: str = _mask_account(str(data.get("source_account", "")))
    destination_account: str = _mask_account(str(data.get("destination_account", "")))

    logger.info(
        "timestamp=%s txn_id=%s status=%s source=%s destination=%s",
        datetime.now(timezone.utc).isoformat(),
        transaction_id,
        status,
        source_account,
        destination_account,
    )


def run_pipeline() -> dict[str, int]:
    _configure_logging()
    logger: logging.Logger = logging.getLogger(LOGGER_NAME)

    _ensure_shared_directories()
    transactions: list[dict[str, Any]] = _load_transactions(SAMPLE_TRANSACTIONS_PATH)

    summary: dict[str, int] = {
        "total": 0,
        "settled": 0,
        "pending_review": 0,
        "rejected": 0,
    }

    for transaction in transactions:
        message: dict[str, Any] = _build_message(transaction)
        transaction_id: str = str(transaction.get("transaction_id", "UNKNOWN"))

        _write_json(INPUT_DIR / f"{transaction_id}.json", message)

        message = _run_stage(
            message=message,
            stage_name="validator",
            source_agent="integrator",
            target_agent="transaction_validator",
            stage_function=validator_process_message,
        )

        message = _run_stage(
            message=message,
            stage_name="fraud_detector",
            source_agent="transaction_validator",
            target_agent="fraud_detector",
            stage_function=fraud_process_message,
        )

        message = _run_stage(
            message=message,
            stage_name="settlement_processor",
            source_agent="fraud_detector",
            target_agent="settlement_processor",
            stage_function=settlement_process_message,
        )

        _write_json(RESULTS_DIR / f"{transaction_id}.json", message)

        classification: str = _classify_result(message)
        summary["total"] += 1
        summary[classification] += 1

        _log_pipeline_step(logger, message)

    print("Pipeline Summary")
    print(f"total={summary['total']}")
    print(f"settled={summary['settled']}")
    print(f"pending_review={summary['pending_review']}")
    print(f"rejected={summary['rejected']}")

    return summary


if __name__ == "__main__":
    run_pipeline()
