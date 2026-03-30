"""Unit tests for agents/settlement_processor.py."""
from __future__ import annotations

from decimal import Decimal
from typing import Any

import pytest

from agents.settlement_processor import (
    SettlementProcessor,
    _configure_logging,
    process_message,
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_message(data: dict[str, Any]) -> dict[str, Any]:
    return {
        "message_id": "msg-test-sp",
        "timestamp": "2026-03-16T10:00:00Z",
        "source_agent": "fraud_detector",
        "target_agent": "settlement_processor",
        "message_type": "transaction",
        "data": data,
    }


def _validated_data(
    txn_id: str = "TXN001",
    amount: str = "1500.00",
    fraud_risk_level: str = "LOW",
    **extra: Any,
) -> dict[str, Any]:
    base: dict[str, Any] = {
        "transaction_id": txn_id,
        "status": "validated",
        "amount": amount,
        "currency": "USD",
        "source_account": "ACC-1001",
        "destination_account": "ACC-2001",
        "fraud_risk_level": fraud_risk_level,
        "fraud_risk_score": 0,
    }
    base.update(extra)
    return base


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def processor() -> SettlementProcessor:
    return SettlementProcessor()


# ---------------------------------------------------------------------------
# Settlement decisions by risk level
# ---------------------------------------------------------------------------

def test_low_risk_sets_settled_status() -> None:
    result = process_message(_make_message(_validated_data(fraud_risk_level="LOW")))
    assert result["data"]["status"] == "settled"


def test_low_risk_settlement_note_is_settled() -> None:
    result = process_message(_make_message(_validated_data(fraud_risk_level="LOW")))
    assert result["data"]["settlement_note"] == "Settled"


def test_medium_risk_sets_settled_status() -> None:
    result = process_message(_make_message(_validated_data(fraud_risk_level="MEDIUM", fraud_risk_score=3)))
    assert result["data"]["status"] == "settled"


def test_medium_risk_settlement_note_mentions_monitoring() -> None:
    result = process_message(_make_message(_validated_data(fraud_risk_level="MEDIUM", fraud_risk_score=3)))
    assert "monitoring" in result["data"]["settlement_note"].lower()


def test_high_risk_sets_held_status() -> None:
    result = process_message(
        _make_message(_validated_data(txn_id="TXN005", amount="75000.00",
                                      fraud_risk_level="HIGH", fraud_risk_score=7))
    )
    assert result["data"]["status"] == "held"


def test_high_risk_settlement_note_mentions_review() -> None:
    result = process_message(
        _make_message(_validated_data(fraud_risk_level="HIGH", fraud_risk_score=7))
    )
    assert "review" in result["data"]["settlement_note"].lower()


def test_missing_fraud_risk_level_defaults_to_low_path() -> None:
    # When fraud_risk_level is absent, str(data.get(..., "LOW")) → "LOW" branch
    data = _validated_data()
    del data["fraud_risk_level"]
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "settled"
    assert result["data"]["settlement_note"] == "Settled"


# ---------------------------------------------------------------------------
# Rejected pass-through
# ---------------------------------------------------------------------------

def test_rejected_transaction_passes_through_unchanged() -> None:
    data: dict[str, Any] = {
        "transaction_id": "TXN006",
        "status": "rejected",
        "reason": "INVALID_CURRENCY",
        "amount": "200.00",
        "currency": "XYZ",
        "source_account": "ACC-1006",
        "destination_account": "ACC-7700",
    }
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_CURRENCY"
    assert "settlement_note" not in result["data"]
    assert "processed_at" not in result["data"]
    assert "processing_agent" not in result["data"]


def test_rejected_txn007_passes_through() -> None:
    data: dict[str, Any] = {
        "transaction_id": "TXN007",
        "status": "rejected",
        "reason": "INVALID_AMOUNT",
        "amount": "-100.00",
        "currency": "GBP",
        "source_account": "ACC-1007",
        "destination_account": "ACC-8800",
    }
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"


# ---------------------------------------------------------------------------
# Amount normalisation
# ---------------------------------------------------------------------------

def test_amount_without_cents_padded_to_two_decimal_places() -> None:
    result = process_message(_make_message(_validated_data(amount="1500.5")))
    assert result["data"]["amount"] == "1500.50"


def test_amount_rounded_half_up_third_decimal() -> None:
    # 1.555 → should round to 1.56 with ROUND_HALF_UP
    result = process_message(_make_message(_validated_data(amount="1.555")))
    assert result["data"]["amount"] == "1.56"


def test_amount_rounded_half_up_half_away_from_zero() -> None:
    # 2.445 → 2.45 with ROUND_HALF_UP
    result = process_message(_make_message(_validated_data(amount="2.445")))
    assert result["data"]["amount"] == "2.45"


def test_amount_already_two_decimals_unchanged_value() -> None:
    result = process_message(_make_message(_validated_data(amount="100.00")))
    assert Decimal(result["data"]["amount"]) == Decimal("100.00")


# ---------------------------------------------------------------------------
# Metadata added by processor
# ---------------------------------------------------------------------------

def test_processed_at_is_iso8601_format() -> None:
    result = process_message(_make_message(_validated_data()))
    processed_at = result["data"]["processed_at"]
    assert "T" in processed_at  # ISO 8601 separator
    assert processed_at.endswith("+00:00") or processed_at.endswith("Z")


def test_processing_agent_name_is_settlement_processor() -> None:
    result = process_message(_make_message(_validated_data()))
    assert result["data"]["processing_agent"] == "settlement_processor"


# ---------------------------------------------------------------------------
# Deep-copy isolation
# ---------------------------------------------------------------------------

def test_original_message_not_mutated() -> None:
    data = _validated_data()
    msg = _make_message(data)
    original_amount = msg["data"]["amount"]
    original_status = msg["data"]["status"]
    process_message(msg)
    assert msg["data"]["amount"] == original_amount
    assert msg["data"]["status"] == original_status
    assert "processed_at" not in msg["data"]


# ---------------------------------------------------------------------------
# Mask account helper
# ---------------------------------------------------------------------------

def test_mask_account_normal(processor: SettlementProcessor) -> None:
    assert processor._mask_account("ACC-9900") == "****9900"


def test_mask_account_empty_returns_stars(processor: SettlementProcessor) -> None:
    assert processor._mask_account("") == "****"


# ---------------------------------------------------------------------------
# Module helpers
# ---------------------------------------------------------------------------

def test_configure_logging_does_not_raise() -> None:
    _configure_logging()
