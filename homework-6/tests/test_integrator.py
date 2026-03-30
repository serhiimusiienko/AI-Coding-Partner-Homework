"""Tests for integrator.py — unit helpers + full pipeline integration."""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import pytest

import integrator
from integrator import (
    _build_message,
    _classify_result,
    _load_transactions,
    _mask_account,
    run_pipeline,
)


# ---------------------------------------------------------------------------
# _mask_account
# ---------------------------------------------------------------------------

def test_mask_account_returns_last_four() -> None:
    assert _mask_account("ACC-1234") == "****1234"


def test_mask_account_empty_string() -> None:
    assert _mask_account("") == "****"


def test_mask_account_short_string() -> None:
    assert _mask_account("AB") == "****AB"


# ---------------------------------------------------------------------------
# _build_message
# ---------------------------------------------------------------------------

def test_build_message_contains_all_envelope_keys() -> None:
    txn = {"transaction_id": "TXN001", "amount": "100.00", "currency": "USD"}
    msg = _build_message(txn)
    for key in ("message_id", "timestamp", "source_agent", "target_agent",
                "message_type", "data"):
        assert key in msg, f"missing key: {key}"


def test_build_message_data_is_deep_copy() -> None:
    txn: dict[str, Any] = {"transaction_id": "T1", "amount": "5.00"}
    msg = _build_message(txn)
    msg["data"]["transaction_id"] = "MUTATED"
    assert txn["transaction_id"] == "T1"


def test_build_message_source_agent_is_integrator() -> None:
    msg = _build_message({"transaction_id": "T1"})
    assert msg["source_agent"] == "integrator"


def test_build_message_target_agent_is_validator() -> None:
    msg = _build_message({"transaction_id": "T1"})
    assert msg["target_agent"] == "transaction_validator"


# ---------------------------------------------------------------------------
# _load_transactions
# ---------------------------------------------------------------------------

def test_load_transactions_returns_list_of_dicts(tmp_path: Path) -> None:
    txns = [{"transaction_id": "T1"}, {"transaction_id": "T2"}]
    p = tmp_path / "txns.json"
    p.write_text(json.dumps(txns))
    loaded = _load_transactions(p)
    assert isinstance(loaded, list)
    assert len(loaded) == 2
    assert loaded[0]["transaction_id"] == "T1"


def test_load_transactions_skips_non_dict_items(tmp_path: Path) -> None:
    p = tmp_path / "mixed.json"
    p.write_text(json.dumps([{"transaction_id": "T1"}, "not-a-dict", 42]))
    loaded = _load_transactions(p)
    assert len(loaded) == 1


def test_load_transactions_raises_on_non_list_json(tmp_path: Path) -> None:
    p = tmp_path / "bad.json"
    p.write_text('{"key": "value"}')
    with pytest.raises(ValueError, match="Expected a list"):
        _load_transactions(p)


# ---------------------------------------------------------------------------
# _classify_result
# ---------------------------------------------------------------------------

def _result_msg(status: str, settlement_status: str = "") -> dict[str, Any]:
    data: dict[str, Any] = {"status": status}
    if settlement_status:
        data["settlement_status"] = settlement_status
    return {"data": data}


def test_classify_result_settled_returns_settled() -> None:
    assert _classify_result(_result_msg("settled")) == "settled"


def test_classify_result_rejected_returns_rejected() -> None:
    assert _classify_result(_result_msg("rejected")) == "rejected"


def test_classify_result_held_returns_pending_review() -> None:
    assert _classify_result(_result_msg("held")) == "pending_review"


def test_classify_result_settlement_status_settled() -> None:
    assert _classify_result(_result_msg("validated", "SETTLED")) == "settled"


def test_classify_result_settlement_status_pending_review() -> None:
    assert _classify_result(_result_msg("validated", "PENDING_REVIEW")) == "pending_review"


def test_classify_result_unknown_status_defaults_to_pending() -> None:
    assert _classify_result(_result_msg("processing")) == "pending_review"


def test_classify_result_empty_message_returns_pending() -> None:
    assert _classify_result({}) == "pending_review"


# ---------------------------------------------------------------------------
# Integration: run_pipeline() with monkeypatched shared dirs
# ---------------------------------------------------------------------------

@pytest.fixture
def isolated_pipeline(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> Path:
    """Redirect all integrator directory constants to tmp_path subdirs."""
    shared = tmp_path / "shared"
    monkeypatch.setattr(integrator, "INPUT_DIR", shared / "input")
    monkeypatch.setattr(integrator, "PROCESSING_DIR", shared / "processing")
    monkeypatch.setattr(integrator, "OUTPUT_DIR", shared / "output")
    monkeypatch.setattr(integrator, "RESULTS_DIR", shared / "results")
    return shared / "results"


def test_run_pipeline_creates_eight_result_files(isolated_pipeline: Path) -> None:
    summary = run_pipeline()
    result_files = list(isolated_pipeline.glob("*.json"))
    assert len(result_files) == 8
    assert summary["total"] == 8


def test_run_pipeline_summary_settled_count(isolated_pipeline: Path) -> None:
    summary = run_pipeline()
    assert summary["settled"] == 5


def test_run_pipeline_summary_rejected_count(isolated_pipeline: Path) -> None:
    summary = run_pipeline()
    assert summary["rejected"] == 2


def test_run_pipeline_summary_pending_review_count(isolated_pipeline: Path) -> None:
    summary = run_pipeline()
    assert summary["pending_review"] == 1


def test_run_pipeline_txn006_is_rejected(isolated_pipeline: Path) -> None:
    run_pipeline()
    result_path = isolated_pipeline / "TXN006.json"
    assert result_path.exists()
    data = json.loads(result_path.read_text())["data"]
    assert data["status"] == "rejected"
    assert data["reason"] == "INVALID_CURRENCY"


def test_run_pipeline_txn007_is_rejected(isolated_pipeline: Path) -> None:
    run_pipeline()
    result_path = isolated_pipeline / "TXN007.json"
    assert result_path.exists()
    data = json.loads(result_path.read_text())["data"]
    assert data["status"] == "rejected"
    assert data["reason"] == "INVALID_AMOUNT"


def test_run_pipeline_txn005_is_held(isolated_pipeline: Path) -> None:
    # TXN005: $75k → HIGH fraud risk → held
    run_pipeline()
    result_path = isolated_pipeline / "TXN005.json"
    data = json.loads(result_path.read_text())["data"]
    assert data["status"] == "held"


def test_run_pipeline_result_files_have_processing_agent(isolated_pipeline: Path) -> None:
    run_pipeline()
    # All non-rejected results should have processing_agent set
    for path in isolated_pipeline.glob("*.json"):
        data = json.loads(path.read_text())["data"]
        if data.get("status") != "rejected":
            assert data.get("processing_agent") == "settlement_processor"


def test_run_pipeline_txn001_low_risk_settled(isolated_pipeline: Path) -> None:
    run_pipeline()
    result_path = isolated_pipeline / "TXN001.json"
    data = json.loads(result_path.read_text())["data"]
    assert data["status"] == "settled"
    assert data["fraud_risk_level"] == "LOW"
