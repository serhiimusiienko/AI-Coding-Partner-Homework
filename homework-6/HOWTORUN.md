# How to Run — AI-Powered Multi-Agent Banking Pipeline

## Prerequisites

- Python 3.14+
- [uv](https://docs.astral.sh/uv/) installed (`curl -Lsf https://astral.sh/uv/install.sh | sh`)

---

## Steps

### 1. Install dependencies

```bash
cd homework-6
uv sync
```

### 2. Run the full pipeline

```bash
uv run python integrator.py
```

Expected output:
```
Pipeline complete — total: 8 | settled: 5 | pending_review: 1 | rejected: 2
```

Results are written to `shared/results/TXNxxx.json`.

### 3. Validate transactions only (dry-run, no side effects)

```bash
uv run python agents/transaction_validator.py --dry-run
```

Prints a table showing which transactions pass or fail and why.

### 4. Run tests with coverage

```bash
uv run pytest --cov=. --cov-report=term-missing
```

Coverage must be ≥ 80% for `git push` to succeed (enforced by hook).

### 5. Start the MCP server

```bash
uv run python mcp/server.py
```

The server exposes:
- Tool `get_transaction_status` — query a single transaction by ID
- Tool `list_pipeline_results` — summary of all results
- Resource `pipeline://summary` — human-readable pipeline report

Configure `mcp.json` in your editor to connect (already pre-configured for VS Code / Claude Code).

---

## Quick reference

| Command | Purpose |
|---|---|
| `uv sync` | Install all dependencies |
| `uv run python integrator.py` | Run the full pipeline |
| `uv run python agents/transaction_validator.py --dry-run` | Validate only |
| `uv run pytest --cov=. --cov-report=term-missing` | Tests + coverage |
| `uv run python mcp/server.py` | Start MCP server |

---

*Created by Serhii Musiienko as part of the AI-Assisted Development course.*
