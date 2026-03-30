"""Tests for mcp/server.py (imported as 'pipeline_server' via conftest.py).

The conftest.py pre-loads mcp/server.py under the alias 'pipeline_server'
to avoid shadowing the installed 'mcp' PyPI package used by fastmcp.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import pytest

import pipeline_server  # loaded by conftest.pytest_configure


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _write_result(
    results_dir: Path,
    txn_id: str,
    status: str,
    amount: str = "1000.00",
    currency: str = "USD",
    fraud_risk_level: str = "LOW",
    reason: str = "",
    settlement_note: str = "Settled",
) -> Path:
    """Write a fake pipeline result JSON file."""
    data: dict[str, Any] = {
        "transaction_id": txn_id,
        "status": status,
        "amount": amount,
        "currency": currency,
        "fraud_risk_level": fraud_risk_level,
        "reason": reason,
        "settlement_note": settlement_note,
        "processed_at": "2026-03-16T10:00:00+00:00",
        "processing_agent": "settlement_processor",
    }
    path = results_dir / f"{txn_id}.json"
    path.write_text(json.dumps({"data": data}))
    return path


# ---------------------------------------------------------------------------
# get_transaction_status
# ---------------------------------------------------------------------------

def test_get_transaction_status_returns_correct_data(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN001", "settled", amount="1500.00", currency="USD",
                  fraud_risk_level="LOW")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.get_transaction_status("TXN001")

    assert result["transaction_id"] == "TXN001"
    assert result["status"] == "settled"
    assert result["amount"] == "1500.00"
    assert result["currency"] == "USD"
    assert result["fraud_risk_level"] == "LOW"


def test_get_transaction_status_not_found_returns_error(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.get_transaction_status("MISSING_TXN")

    assert "error" in result
    assert "MISSING_TXN" in result["error"]
    assert result["transaction_id"] == "MISSING_TXN"


def test_get_transaction_status_rejected_shows_reason(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN006", "rejected", reason="INVALID_CURRENCY", currency="XYZ")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.get_transaction_status("TXN006")

    assert result["status"] == "rejected"
    assert result["reason"] == "INVALID_CURRENCY"


def test_get_transaction_status_held_transaction(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN005", "held", amount="75000.00",
                  fraud_risk_level="HIGH", settlement_note="Held for manual review")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.get_transaction_status("TXN005")

    assert result["status"] == "held"
    assert result["fraud_risk_level"] == "HIGH"
    assert "manual review" in result["settlement_note"].lower()


# ---------------------------------------------------------------------------
# list_pipeline_results
# ---------------------------------------------------------------------------

def test_list_pipeline_results_empty_directory(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.list_pipeline_results()

    assert result["total"] == 0
    assert result["transactions"] == []
    assert "generated_at" in result


def test_list_pipeline_results_nonexistent_directory(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path / "no_such_dir")

    result = pipeline_server.list_pipeline_results()

    assert result["total"] == 0


def test_list_pipeline_results_counts_by_status(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN001", "settled")
    _write_result(tmp_path, "TXN002", "settled")
    _write_result(tmp_path, "TXN005", "held", fraud_risk_level="HIGH")
    _write_result(tmp_path, "TXN006", "rejected", reason="INVALID_CURRENCY")
    _write_result(tmp_path, "TXN007", "rejected", reason="INVALID_AMOUNT")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.list_pipeline_results()

    assert result["total"] == 5
    assert result["counts"]["settled"] == 2
    assert result["counts"]["held"] == 1
    assert result["counts"]["rejected"] == 2


def test_list_pipeline_results_transactions_list_has_correct_length(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN001", "settled")
    _write_result(tmp_path, "TXN002", "settled")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.list_pipeline_results()

    assert len(result["transactions"]) == 2


def test_list_pipeline_results_skips_invalid_json(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    (tmp_path / "bad.json").write_text("not-valid-json{{")
    _write_result(tmp_path, "TXN001", "settled")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.list_pipeline_results()

    # bad.json is skipped; only TXN001 counts
    assert result["total"] == 1


def test_list_pipeline_results_unknown_status_counted_as_other(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    # Write a result with a status not in settled/held/rejected
    data = {"data": {"transaction_id": "T1", "status": "processing"}}
    (tmp_path / "T1.json").write_text(json.dumps(data))
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    result = pipeline_server.list_pipeline_results()

    assert result["counts"]["other"] == 1


# ---------------------------------------------------------------------------
# pipeline_summary resource
# ---------------------------------------------------------------------------

def test_pipeline_summary_no_results_returns_run_message(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    summary = pipeline_server.pipeline_summary()

    assert "No pipeline results" in summary


def test_pipeline_summary_contains_counts(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN001", "settled")
    _write_result(tmp_path, "TXN005", "held", fraud_risk_level="HIGH")
    _write_result(tmp_path, "TXN006", "rejected", reason="INVALID_CURRENCY")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    summary = pipeline_server.pipeline_summary()

    assert "Settled" in summary
    assert "Held" in summary
    assert "Rejected" in summary


def test_pipeline_summary_lists_rejected_transactions(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN006", "rejected", reason="INVALID_CURRENCY")
    _write_result(tmp_path, "TXN007", "rejected", reason="INVALID_AMOUNT")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    summary = pipeline_server.pipeline_summary()

    assert "TXN006" in summary
    assert "INVALID_CURRENCY" in summary
    assert "TXN007" in summary
    assert "INVALID_AMOUNT" in summary


def test_pipeline_summary_unknown_status_counted_as_settled(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    # Status not in settled/held/rejected → falls into the else branch (counts as settled)
    data = {"data": {"transaction_id": "T1", "status": "processing"}}
    (tmp_path / "T1.json").write_text(json.dumps(data))
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    summary = pipeline_server.pipeline_summary()

    assert "Total processed" in summary


def test_pipeline_summary_generated_at_present(
    tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    _write_result(tmp_path, "TXN001", "settled")
    monkeypatch.setattr(pipeline_server, "RESULTS_DIR", tmp_path)

    summary = pipeline_server.pipeline_summary()

    assert "Generated at" in summary
