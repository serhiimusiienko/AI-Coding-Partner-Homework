from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from fastmcp import FastMCP

mcp: FastMCP = FastMCP(name="pipeline-status")

# shared/results/ lives two directories above this file (homework-6/shared/results/).
RESULTS_DIR: Path = Path(__file__).resolve().parent.parent / "shared" / "results"


def _load_result(transaction_id: str) -> dict[str, Any] | None:
    """Load a single result file by transaction_id. Returns None if not found."""
    result_path: Path = RESULTS_DIR / f"{transaction_id}.json"
    if not result_path.exists():
        return None
    with result_path.open("r", encoding="utf-8") as f:
        return json.load(f)


def _load_all_results() -> list[dict[str, Any]]:
    """Load every result file from shared/results/."""
    results: list[dict[str, Any]] = []
    if not RESULTS_DIR.exists():
        return results
    for path in sorted(RESULTS_DIR.glob("*.json")):
        try:
            with path.open("r", encoding="utf-8") as f:
                results.append(json.load(f))
        except (json.JSONDecodeError, OSError):
            pass
    return results


@mcp.tool
def get_transaction_status(transaction_id: str) -> dict[str, Any]:
    """Return the current status of a single transaction from shared/results/."""
    message: dict[str, Any] | None = _load_result(transaction_id)
    if message is None:
        return {
            "error": f"Transaction {transaction_id} not found in shared/results/",
            "transaction_id": transaction_id,
        }

    data: dict[str, Any] = message.get("data", {})
    return {
        "transaction_id": str(data.get("transaction_id", transaction_id)),
        "status": str(data.get("status", "unknown")),
        "amount": str(data.get("amount", "")),
        "currency": str(data.get("currency", "")),
        "fraud_risk_level": str(data.get("fraud_risk_level", "")),
        "settlement_note": str(data.get("settlement_note", "")),
        "reason": str(data.get("reason", "")),
        "processed_at": str(data.get("processed_at", "")),
    }


@mcp.tool
def list_pipeline_results() -> dict[str, Any]:
    """Return a summary of all processed transactions from shared/results/."""
    all_messages: list[dict[str, Any]] = _load_all_results()

    counts: dict[str, int] = {"settled": 0, "held": 0, "rejected": 0, "other": 0}
    transactions: list[dict[str, str]] = []

    for message in all_messages:
        data: dict[str, Any] = message.get("data", {})
        txn_id: str = str(data.get("transaction_id", "UNKNOWN"))
        status: str = str(data.get("status", "unknown")).lower()

        if status in counts:
            counts[status] += 1
        else:
            counts["other"] += 1

        transactions.append({
            "transaction_id": txn_id,
            "status": status,
            "amount": str(data.get("amount", "")),
            "currency": str(data.get("currency", "")),
            "fraud_risk_level": str(data.get("fraud_risk_level", "")),
            "reason": str(data.get("reason", "")),
        })

    return {
        "total": len(all_messages),
        "counts": counts,
        "transactions": transactions,
        "generated_at": datetime.now(timezone.utc).isoformat(),
    }


@mcp.resource("pipeline://summary")
def pipeline_summary() -> str:
    """Return the latest pipeline run summary as plain text."""
    all_messages: list[dict[str, Any]] = _load_all_results()

    if not all_messages:
        return "No pipeline results found. Run the pipeline first: uv run python integrator.py"

    counts: dict[str, int] = {"settled": 0, "held": 0, "rejected": 0}
    rejected_lines: list[str] = []

    for message in all_messages:
        data: dict[str, Any] = message.get("data", {})
        status: str = str(data.get("status", "")).lower()
        txn_id: str = str(data.get("transaction_id", "UNKNOWN"))

        if status in counts:
            counts[status] += 1
        else:
            counts["settled"] += 1  # treat unknown as settled for summary

        if status == "rejected":
            reason: str = str(data.get("reason", "UNKNOWN_REASON"))
            rejected_lines.append(f"  - {txn_id}: {reason}")

    lines: list[str] = [
        "=== Pipeline Run Summary ===",
        f"Total processed : {len(all_messages)}",
        f"Settled         : {counts['settled']}",
        f"Held (review)   : {counts['held']}",
        f"Rejected        : {counts['rejected']}",
    ]

    if rejected_lines:
        lines.append("\nRejected transactions:")
        lines.extend(rejected_lines)

    lines.append(f"\nGenerated at: {datetime.now(timezone.utc).isoformat()}")
    return "\n".join(lines)


if __name__ == "__main__":
    mcp.run()
