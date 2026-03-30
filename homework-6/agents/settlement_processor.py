from __future__ import annotations

import logging
from copy import deepcopy
from datetime import datetime, timezone
from decimal import Decimal, ROUND_HALF_UP
from typing import Any

LOG_FORMAT: str = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"
LOGGER_NAME: str = "settlement_processor"


class SettlementProcessor:
    def __init__(self, logger: logging.Logger | None = None) -> None:
        self.logger: logging.Logger = logger or logging.getLogger(LOGGER_NAME)

    def process_message(self, message: dict[str, Any]) -> dict[str, Any]:
        message_copy: dict[str, Any] = deepcopy(message)
        data: dict[str, Any] = self._ensure_data_dict(message_copy)

        transaction_id: str = str(data.get("transaction_id", "UNKNOWN"))
        source_account: str = str(data.get("source_account", ""))
        destination_account: str = str(data.get("destination_account", ""))

        # Pass through rejected transactions unchanged.
        if data.get("status") == "rejected":
            self._log_audit(transaction_id, source_account, destination_account, outcome="rejected")
            return message_copy

        # Make settlement decision based on fraud risk level.
        try:
            fraud_risk_level: str = str(data.get("fraud_risk_level", "LOW"))
            
            if fraud_risk_level == "HIGH":
                data["status"] = "held"
                data["settlement_note"] = "Held for manual review"
            elif fraud_risk_level == "MEDIUM":
                data["status"] = "settled"
                data["settlement_note"] = "Settled with enhanced monitoring"
            else:  # LOW
                data["status"] = "settled"
                data["settlement_note"] = "Settled"

            # Normalize amount to 2 decimal places using ROUND_HALF_UP.
            amount_str: str = str(data.get("amount", "0"))
            amount: Decimal = Decimal(amount_str)
            normalized_amount: Decimal = amount.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            data["amount"] = str(normalized_amount)

        except Exception:
            self.logger.exception("Unexpected settlement failure for transaction %s", transaction_id)
            data["status"] = "held"
            data["settlement_note"] = "Error during settlement processing"

        # Add processing metadata.
        data["processed_at"] = datetime.now(timezone.utc).isoformat()
        data["processing_agent"] = LOGGER_NAME

        self._log_audit(transaction_id, source_account, destination_account, outcome=data["status"])
        return message_copy

    def _ensure_data_dict(self, message: dict[str, Any]) -> dict[str, Any]:
        data: Any = message.get("data")
        if not isinstance(data, dict):
            data = {}
            message["data"] = data
        return data

    def _log_audit(
        self,
        transaction_id: str,
        source_account: str,
        destination_account: str,
        outcome: str,
    ) -> None:
        masked_source: str = self._mask_account(source_account)
        masked_destination: str = self._mask_account(destination_account)
        audit_timestamp: str = datetime.now(timezone.utc).isoformat()
        self.logger.info(
            "timestamp=%s txn_id=%s outcome=%s source=%s destination=%s",
            audit_timestamp,
            transaction_id,
            outcome,
            masked_source,
            masked_destination,
        )

    def _mask_account(self, account_number: str) -> str:
        if account_number == "":
            return "****"
        return f"****{account_number[-4:]}"


def process_message(message: dict[str, Any]) -> dict[str, Any]:
    processor: SettlementProcessor = SettlementProcessor()
    return processor.process_message(message)


def _configure_logging() -> None:
    root_logger: logging.Logger = logging.getLogger()
    if not root_logger.handlers:
        logging.basicConfig(
            level=logging.INFO,
            format=LOG_FORMAT,
            datefmt="%Y-%m-%dT%H:%M:%S%z",
        )


if __name__ == "__main__":
    print("Settlement Processor agent module. Use process_message() to settle a transaction.")
