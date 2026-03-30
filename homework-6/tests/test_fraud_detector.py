"""Unit tests for agents/fraud_detector.py."""
from __future__ import annotations

from typing import Any

import pytest

from agents.fraud_detector import FraudDetector, _configure_logging, process_message


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_message(data: dict[str, Any], status: str = "validated") -> dict[str, Any]:
    return {
        "message_id": "msg-test-fd",
        "timestamp": "2026-03-16T09:00:00Z",
        "source_agent": "transaction_validator",
        "target_agent": "fraud_detector",
        "message_type": "transaction",
        "data": {"status": status, **data},
    }


def _validated(txn_id: str, amount: str, timestamp: str, country: str = "US") -> dict[str, Any]:
    return {
        "transaction_id": txn_id,
        "amount": amount,
        "currency": "USD",
        "source_account": f"ACC-{txn_id}",
        "destination_account": "ACC-9999",
        "timestamp": timestamp,
        "metadata": {"country": country},
    }


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def detector() -> FraudDetector:
    return FraudDetector()


# ---------------------------------------------------------------------------
# Pass-through: rejected transactions
# ---------------------------------------------------------------------------

def test_rejected_transaction_passes_through_without_fraud_fields() -> None:
    msg = _make_message(
        {"transaction_id": "TXN007", "amount": "-100.00", "currency": "GBP",
         "source_account": "ACC-1007", "destination_account": "ACC-8800"},
        status="rejected",
    )
    result = process_message(msg)
    assert result["data"]["status"] == "rejected"
    assert "fraud_risk_score" not in result["data"]
    assert "fraud_risk_level" not in result["data"]


def test_rejected_transaction_reason_preserved() -> None:
    msg = _make_message(
        {"transaction_id": "TXN006", "reason": "INVALID_CURRENCY"},
        status="rejected",
    )
    result = process_message(msg)
    assert result["data"]["reason"] == "INVALID_CURRENCY"


# ---------------------------------------------------------------------------
# Low risk (score 0–2)
# ---------------------------------------------------------------------------

def test_low_risk_small_us_daytime_transaction() -> None:
    # TXN001: $1500, US, 09:00 UTC → score 0 → LOW
    msg = _make_message(_validated("TXN001", "1500.00", "2026-03-16T09:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_level"] == "LOW"
    assert result["data"]["fraud_risk_score"] == 0


def test_cross_border_only_adds_one_point() -> None:
    # $5000 at 10:00 UTC, DE → +0 (amount) +0 (hour) +1 (DE) = 1 → LOW
    msg = _make_message(_validated("T1", "5000.00", "2026-03-16T10:00:00Z", "DE"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 1
    assert result["data"]["fraud_risk_level"] == "LOW"


def test_night_hour_02_utc_adds_two_points() -> None:
    # $500 at 02:00 UTC, US → +0 (amount) +2 (night) +0 (US) = 2 → LOW
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T02:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 2
    assert result["data"]["fraud_risk_level"] == "LOW"


def test_night_hour_05_utc_boundary_adds_two_points() -> None:
    # 05:00 UTC is within the 02–05 window
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T05:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 2


def test_hour_06_utc_does_not_add_night_bonus() -> None:
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T06:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


def test_hour_01_utc_does_not_add_night_bonus() -> None:
    # 01:00 is before the 02–05 window
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T01:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


# ---------------------------------------------------------------------------
# Medium risk (score 3–6)
# ---------------------------------------------------------------------------

def test_medium_risk_amount_over_10k() -> None:
    # TXN002: $25000, US, 09:15 UTC → +3 (amount>10k) = 3 → MEDIUM
    msg = _make_message(_validated("TXN002", "25000.00", "2026-03-16T09:15:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_level"] == "MEDIUM"
    assert result["data"]["fraud_risk_score"] == 3


def test_txn004_night_and_cross_border_scores_medium() -> None:
    # TXN004: $500, DE, 02:47 UTC → +0 (amount) +2 (night) +1 (DE) = 3 → MEDIUM
    msg = _make_message(_validated("TXN004", "500.00", "2026-03-16T02:47:00Z", "DE"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 3
    assert result["data"]["fraud_risk_level"] == "MEDIUM"


def test_over_10k_plus_night_plus_cross_border_scores_six() -> None:
    # $25k +3, night +2, DE +1 = 6 → MEDIUM
    msg = _make_message(_validated("T1", "25000.00", "2026-03-16T03:00:00Z", "DE"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 6
    assert result["data"]["fraud_risk_level"] == "MEDIUM"


# ---------------------------------------------------------------------------
# High risk (score 7–10)
# ---------------------------------------------------------------------------

def test_high_risk_amount_over_50k() -> None:
    # TXN005: $75000, US, 10:00 UTC → +7 (>50k) = 7 → HIGH
    msg = _make_message(_validated("TXN005", "75000.00", "2026-03-16T10:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_level"] == "HIGH"
    assert result["data"]["fraud_risk_score"] == 7


def test_score_capped_at_ten() -> None:
    # $75k (+7) + night 03:00 (+2) + DE (+1) = 10 → capped at 10 → HIGH
    msg = _make_message(_validated("T1", "75000.00", "2026-03-16T03:00:00Z", "DE"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 10
    assert result["data"]["fraud_risk_level"] == "HIGH"


def test_amount_exactly_50k_is_not_over_50k() -> None:
    # Exactly $50000 → NOT > 50000, so +3 (>10k), not +7
    msg = _make_message(_validated("T1", "50000.00", "2026-03-16T09:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 3


def test_amount_exactly_10k_is_not_over_10k() -> None:
    # Exactly $10000 → NOT > 10000, so +0
    msg = _make_message(_validated("T1", "10000.00", "2026-03-16T09:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


# ---------------------------------------------------------------------------
# Edge cases for timestamp / metadata
# ---------------------------------------------------------------------------

def test_invalid_timestamp_string_does_not_raise() -> None:
    data = _validated("T1", "500.00", "not-a-timestamp")
    msg = _make_message(data)
    result = process_message(msg)
    assert "fraud_risk_level" in result["data"]


def test_missing_timestamp_does_not_raise() -> None:
    data: dict[str, Any] = {
        "transaction_id": "T1", "amount": "500.00",
        "source_account": "A", "destination_account": "B",
        "metadata": {"country": "US"},
    }
    msg = _make_message(data)
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


def test_missing_metadata_no_cross_border_bonus() -> None:
    data: dict[str, Any] = {
        "transaction_id": "T1", "amount": "500.00",
        "timestamp": "2026-03-16T09:00:00Z",
    }
    msg = _make_message(data)
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


def test_non_dict_metadata_no_crash() -> None:
    data: dict[str, Any] = {
        "transaction_id": "T1", "amount": "500.00",
        "timestamp": "2026-03-16T09:00:00Z",
        "metadata": "invalid",
    }
    msg = _make_message(data)
    result = process_message(msg)
    assert "fraud_risk_level" in result["data"]


def test_us_country_no_cross_border_bonus() -> None:
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T09:00:00Z", "US"))
    result = process_message(msg)
    assert result["data"]["fraud_risk_score"] == 0


# ---------------------------------------------------------------------------
# Deep-copy isolation
# ---------------------------------------------------------------------------

def test_original_message_not_mutated_by_process_message() -> None:
    msg = _make_message(_validated("T1", "500.00", "2026-03-16T09:00:00Z"))
    original_status = msg["data"]["status"]
    process_message(msg)
    assert msg["data"]["status"] == original_status
    assert "fraud_risk_score" not in msg["data"]


# ---------------------------------------------------------------------------
# Mask account helper
# ---------------------------------------------------------------------------

def test_mask_account_normal(detector: FraudDetector) -> None:
    assert detector._mask_account("ACC-1234") == "****1234"


def test_mask_account_empty(detector: FraudDetector) -> None:
    assert detector._mask_account("") == "****"


# ---------------------------------------------------------------------------
# Module helpers
# ---------------------------------------------------------------------------

def test_configure_logging_is_idempotent() -> None:
    _configure_logging()
    _configure_logging()  # calling twice should not raise
