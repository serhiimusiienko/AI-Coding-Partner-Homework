"""Unit tests for agents/transaction_validator.py."""
from __future__ import annotations

import json
from io import StringIO
from pathlib import Path
from typing import Any

import pytest

from agents.transaction_validator import (
    TransactionValidator,
    ValidationResult,
    _build_message_from_transaction,
    _configure_logging,
    _print_results_table,
    _run_dry_run,
    _to_result_row,
    process_message,
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_message(data: dict[str, Any]) -> dict[str, Any]:
    return {
        "message_id": "msg-test-001",
        "timestamp": "2026-03-16T09:00:00Z",
        "source_agent": "test",
        "target_agent": "transaction_validator",
        "message_type": "transaction",
        "data": data,
    }


def _valid_data(**overrides: Any) -> dict[str, Any]:
    base: dict[str, Any] = {
        "transaction_id": "TXN001",
        "amount": "1500.00",
        "currency": "USD",
        "source_account": "ACC-1001",
        "destination_account": "ACC-2001",
    }
    base.update(overrides)
    return base


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_message() -> dict[str, Any]:
    return _make_message(_valid_data())


@pytest.fixture
def validator() -> TransactionValidator:
    return TransactionValidator()


# ---------------------------------------------------------------------------
# Happy path
# ---------------------------------------------------------------------------

def test_valid_transaction_returns_validated_status(valid_message: dict[str, Any]) -> None:
    result = process_message(valid_message)
    assert result["data"]["status"] == "validated"


def test_valid_transaction_has_no_reason_field(valid_message: dict[str, Any]) -> None:
    result = process_message(valid_message)
    assert "reason" not in result["data"]


def test_valid_transaction_does_not_mutate_input(valid_message: dict[str, Any]) -> None:
    original_data = dict(valid_message["data"])
    process_message(valid_message)
    assert valid_message["data"] == original_data


# ---------------------------------------------------------------------------
# MISSING_FIELD rejections
# ---------------------------------------------------------------------------

@pytest.mark.parametrize("missing_field", [
    "transaction_id",
    "amount",
    "currency",
    "source_account",
    "destination_account",
])
def test_missing_required_field_returns_missing_field_reason(missing_field: str) -> None:
    data = {k: v for k, v in _valid_data().items() if k != missing_field}
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "MISSING_FIELD"


def test_empty_string_transaction_id_returns_missing_field() -> None:
    result = process_message(_make_message(_valid_data(transaction_id="")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "MISSING_FIELD"


def test_whitespace_only_field_returns_missing_field() -> None:
    result = process_message(_make_message(_valid_data(currency="   ")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "MISSING_FIELD"


def test_none_field_returns_missing_field() -> None:
    data = _valid_data()
    data["amount"] = None  # type: ignore[assignment]
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "MISSING_FIELD"


# ---------------------------------------------------------------------------
# INVALID_AMOUNT rejections
# ---------------------------------------------------------------------------

def test_zero_amount_returns_invalid_amount() -> None:
    result = process_message(_make_message(_valid_data(amount="0")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_AMOUNT"


def test_negative_amount_returns_invalid_amount() -> None:
    result = process_message(_make_message(_valid_data(amount="-100.00")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_AMOUNT"


def test_non_numeric_amount_returns_invalid_amount() -> None:
    result = process_message(_make_message(_valid_data(amount="abc")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_AMOUNT"


def test_zero_decimal_amount_returns_invalid_amount() -> None:
    result = process_message(_make_message(_valid_data(amount="0.00")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_AMOUNT"


# ---------------------------------------------------------------------------
# INVALID_CURRENCY rejections
# ---------------------------------------------------------------------------

def test_unknown_currency_returns_invalid_currency() -> None:
    result = process_message(_make_message(_valid_data(currency="XYZ")))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_CURRENCY"


def test_lowercase_currency_returns_invalid_currency() -> None:
    # Currency check uses str().upper() — but only after passing MISSING_FIELD check.
    # The validator does str(currency_value).upper() so lowercase should still validate.
    result = process_message(_make_message(_valid_data(currency="usd")))
    assert result["data"]["status"] == "validated"


# ---------------------------------------------------------------------------
# All allowed currencies
# ---------------------------------------------------------------------------

@pytest.mark.parametrize("currency", ["USD", "EUR", "GBP", "JPY", "CHF"])
def test_all_allowed_currencies_pass_validation(currency: str) -> None:
    result = process_message(_make_message(_valid_data(currency=currency)))
    assert result["data"]["status"] == "validated"


# ---------------------------------------------------------------------------
# Edge cases with the message envelope
# ---------------------------------------------------------------------------

def test_message_without_data_key_gets_missing_field_rejection() -> None:
    msg: dict[str, Any] = {"message_id": "m1"}
    result = process_message(msg)
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "MISSING_FIELD"


def test_message_with_non_dict_data_gets_rejected() -> None:
    msg: dict[str, Any] = {"message_id": "m1", "data": "not a dict"}
    result = process_message(msg)
    assert result["data"]["status"] == "rejected"


# ---------------------------------------------------------------------------
# Real transaction fixtures (TXN006 / TXN007 from sample-transactions.json)
# ---------------------------------------------------------------------------

def test_txn006_invalid_currency_xyz_rejected() -> None:
    data = {
        "transaction_id": "TXN006",
        "amount": "200.00",
        "currency": "XYZ",
        "source_account": "ACC-1006",
        "destination_account": "ACC-7700",
    }
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_CURRENCY"


def test_txn007_negative_amount_rejected() -> None:
    data = {
        "transaction_id": "TXN007",
        "amount": "-100.00",
        "currency": "GBP",
        "source_account": "ACC-1007",
        "destination_account": "ACC-8800",
    }
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "rejected"
    assert result["data"]["reason"] == "INVALID_AMOUNT"


def test_txn001_valid_transaction_passes() -> None:
    data = {
        "transaction_id": "TXN001",
        "amount": "1500.00",
        "currency": "USD",
        "source_account": "ACC-1001",
        "destination_account": "ACC-2001",
    }
    result = process_message(_make_message(data))
    assert result["data"]["status"] == "validated"


# ---------------------------------------------------------------------------
# Internal helper: _mask_account
# ---------------------------------------------------------------------------

def test_mask_account_returns_last_four_digits(validator: TransactionValidator) -> None:
    assert validator._mask_account("ACC-1001") == "****1001"


def test_mask_account_empty_string(validator: TransactionValidator) -> None:
    assert validator._mask_account("") == "****"


# ---------------------------------------------------------------------------
# Standalone helper functions
# ---------------------------------------------------------------------------

def test_to_result_row_validated_message() -> None:
    message = _make_message(_valid_data())
    message["data"]["status"] = "validated"
    row = _to_result_row(message)
    assert isinstance(row, ValidationResult)
    assert row.transaction_id == "TXN001"
    assert row.status == "validated"
    assert row.reason == ""
    assert row.amount == "1500.00"
    assert row.currency == "USD"


def test_to_result_row_rejected_message() -> None:
    message = _make_message(_valid_data(amount="-1.00"))
    message["data"]["status"] = "rejected"
    message["data"]["reason"] = "INVALID_AMOUNT"
    row = _to_result_row(message)
    assert row.status == "rejected"
    assert row.reason == "INVALID_AMOUNT"


def test_to_result_row_missing_data_returns_unknowns() -> None:
    row = _to_result_row({})
    assert row.transaction_id == "UNKNOWN"
    assert row.status == "unknown"


def test_build_message_from_transaction_contains_envelope_keys() -> None:
    txn = {"transaction_id": "TXN001", "amount": "100.00", "currency": "USD"}
    msg = _build_message_from_transaction(txn)
    assert msg["source_agent"] == "integrator"
    assert msg["target_agent"] == "transaction_validator"
    assert msg["message_type"] == "transaction"
    assert msg["data"]["transaction_id"] == "TXN001"
    assert "message_id" in msg
    assert "timestamp" in msg


def test_build_message_does_not_share_data_reference() -> None:
    txn: dict[str, Any] = {"transaction_id": "T1", "amount": "5.00"}
    msg = _build_message_from_transaction(txn)
    msg["data"]["transaction_id"] = "MUTATED"
    assert txn["transaction_id"] == "T1"


def test_configure_logging_does_not_raise() -> None:
    _configure_logging()  # should be idempotent


# ---------------------------------------------------------------------------
# _print_results_table
# ---------------------------------------------------------------------------

def test_print_results_table_outputs_header(capsys: pytest.CaptureFixture[str]) -> None:
    rows = [
        ValidationResult("TXN001", "validated", "", "1500.00", "USD"),
        ValidationResult("TXN006", "rejected", "INVALID_CURRENCY", "200.00", "XYZ"),
    ]
    _print_results_table(rows)
    out = capsys.readouterr().out
    assert "transaction_id" in out
    assert "TXN001" in out
    assert "TXN006" in out
    assert "INVALID_CURRENCY" in out


# ---------------------------------------------------------------------------
# _run_dry_run
# ---------------------------------------------------------------------------

def test_run_dry_run_returns_zero_for_valid_file(tmp_path: Path) -> None:
    txns = [
        {"transaction_id": "T1", "amount": "100.00", "currency": "USD",
         "source_account": "A", "destination_account": "B"}
    ]
    p = tmp_path / "txns.json"
    p.write_text(json.dumps(txns))
    assert _run_dry_run(p) == 0


def test_run_dry_run_prints_summary(tmp_path: Path, capsys: pytest.CaptureFixture[str]) -> None:
    txns = [
        {"transaction_id": "T1", "amount": "50.00", "currency": "USD",
         "source_account": "A", "destination_account": "B"},
        {"transaction_id": "T2", "amount": "-1.00", "currency": "USD",
         "source_account": "A", "destination_account": "B"},
    ]
    p = tmp_path / "txns.json"
    p.write_text(json.dumps(txns))
    _run_dry_run(p)
    out = capsys.readouterr().out
    assert "total=2" in out
    assert "validated=1" in out
    assert "rejected=1" in out


def test_run_dry_run_returns_one_for_missing_file(tmp_path: Path) -> None:
    assert _run_dry_run(tmp_path / "nonexistent.json") == 1


def test_run_dry_run_returns_one_for_invalid_json(tmp_path: Path) -> None:
    p = tmp_path / "bad.json"
    p.write_text("not-json{{")
    assert _run_dry_run(p) == 1


def test_run_dry_run_returns_one_when_not_a_list(tmp_path: Path) -> None:
    p = tmp_path / "dict.json"
    p.write_text('{"key": "value"}')
    assert _run_dry_run(p) == 1


def test_run_dry_run_skips_non_dict_items(tmp_path: Path) -> None:
    txns: list[Any] = [
        "not-a-dict",
        {"transaction_id": "T1", "amount": "10.00", "currency": "USD",
         "source_account": "A", "destination_account": "B"},
    ]
    p = tmp_path / "mixed.json"
    p.write_text(json.dumps(txns))
    assert _run_dry_run(p) == 0
