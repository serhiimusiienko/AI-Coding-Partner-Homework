from __future__ import annotations

import logging
from copy import deepcopy
from datetime import datetime, timezone
from decimal import Decimal
from typing import Any

LOG_FORMAT: str = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"
LOGGER_NAME: str = "fraud_detector"


class FraudDetector:
    SCORE_THRESHOLDS: dict[str, tuple[int, int]] = {
        "LOW": (0, 2),
        "MEDIUM": (3, 6),
        "HIGH": (7, 10),
    }

    def __init__(self, logger: logging.Logger | None = None) -> None:
        self.logger: logging.Logger = logger or logging.getLogger(LOGGER_NAME)

    def process_message(self, message: dict[str, Any]) -> dict[str, Any]:
        message_copy: dict[str, Any] = deepcopy(message)
        data: dict[str, Any] = self._ensure_data_dict(message_copy)

        transaction_id: str = str(data.get("transaction_id", "UNKNOWN"))
        source_account: str = str(data.get("source_account", ""))
        destination_account: str = str(data.get("destination_account", ""))

        # Pass through rejected transactions unchanged.
        if data.get("status") != "validated":
            self._log_audit(transaction_id, source_account, destination_account, status="skipped")
            return message_copy

        # Score the transaction.
        try:
            score: int = self._calculate_fraud_score(data)
            risk_level: str = self._score_to_risk_level(score)
            data["fraud_risk_score"] = score
            data["fraud_risk_level"] = risk_level
        except Exception:
            self.logger.exception("Unexpected scoring failure for transaction %s", transaction_id)
            data["fraud_risk_score"] = 0
            data["fraud_risk_level"] = "LOW"

        self._log_audit(transaction_id, source_account, destination_account, status=data["fraud_risk_level"])
        return message_copy

    def _ensure_data_dict(self, message: dict[str, Any]) -> dict[str, Any]:
        data: Any = message.get("data")
        if not isinstance(data, dict):
            data = {}
            message["data"] = data
        return data

    def _calculate_fraud_score(self, data: dict[str, Any]) -> int:
        score: int = 0

        # Rule 1: Amount > $10,000 → +3 pts; amount > $50,000 → +4 pts additional (total +7).
        amount: Decimal = Decimal(str(data.get("amount", "0")))
        if amount > Decimal("50000"):
            score += 7
        elif amount > Decimal("10000"):
            score += 3

        # Rule 2: Hour in 02–05 UTC → +2 pts.
        timestamp_str: str = str(data.get("timestamp", ""))
        if timestamp_str:
            try:
                timestamp: datetime = datetime.fromisoformat(timestamp_str.replace("Z", "+00:00"))
                hour_utc: int = timestamp.astimezone(timezone.utc).hour
                if 2 <= hour_utc <= 5:
                    score += 2
            except (ValueError, AttributeError):
                self.logger.warning("Could not parse timestamp %s", timestamp_str)

        # Rule 3: Cross-border check (metadata.country != default country) → +1 pt.
        metadata: Any = data.get("metadata", {})
        if isinstance(metadata, dict):
            tx_country: str | None = metadata.get("country")
            if tx_country and tx_country != "US":
                score += 1

        # Cap at 10.
        return min(score, 10)

    def _score_to_risk_level(self, score: int) -> str:
        for level, (min_score, max_score) in self.SCORE_THRESHOLDS.items():
            if min_score <= score <= max_score:
                return level
        return "LOW"

    def _log_audit(
        self,
        transaction_id: str,
        source_account: str,
        destination_account: str,
        status: str,
    ) -> None:
        masked_source: str = self._mask_account(source_account)
        masked_destination: str = self._mask_account(destination_account)
        audit_timestamp: str = datetime.now(timezone.utc).isoformat()
        self.logger.info(
            "timestamp=%s txn_id=%s fraud_risk=%s source=%s destination=%s",
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
    detector: FraudDetector = FraudDetector()
    return detector.process_message(message)


def _configure_logging() -> None:
    root_logger: logging.Logger = logging.getLogger()
    if not root_logger.handlers:
        logging.basicConfig(
            level=logging.INFO,
            format=LOG_FORMAT,
            datefmt="%Y-%m-%dT%H:%M:%S%z",
        )


if __name__ == "__main__":
    print("Fraud Detector agent module. Use process_message() to score a transaction.")
