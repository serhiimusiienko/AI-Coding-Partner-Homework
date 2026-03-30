# 🏦 Homework 6: AI-Powered Multi-Agent Banking Pipeline

> **Student Name**: Serhii Musiienko
> **Date Submitted**: 26.03.2026
> **AI Tools Used**: GitHub Copilot, Claude Code (Claude Opus 4.6, GPT-5.2-Codex, Claude Sonnet 4.6), context7 MCP

---

## 📋 Project Overview

A 3-agent Python pipeline that processes banking transactions through validation, fraud detection, and settlement. Agents communicate via JSON files through shared directories. All 8 sample transactions are processed and written to `shared/results/`.

---

## 🏗️ Pipeline Architecture

```
sample-transactions.json
         │
         ▼
   ┌─────────────┐
   │  integrator │  orchestrates the pipeline
   └──────┬──────┘
          │
          ▼
┌──────────────────────┐
│ transaction_validator│  validates fields, amount, currency
└──────────┬───────────┘
           │  validated / rejected
           ▼
┌──────────────────────┐
│   fraud_detector     │  additive risk scoring → LOW/MEDIUM/HIGH
└──────────┬───────────┘
           │  scored
           ▼
┌──────────────────────┐
│ settlement_processor │  settled / held / pass-through on rejected
└──────────┬───────────┘
           │
           ▼
    shared/results/
     TXN001.json … TXN008.json
```

---

## 👥 Agent Responsibilities

| Agent | File | Responsibility |
|---|---|---|
| Transaction Validator | `agents/transaction_validator.py` | Checks required fields, positive `Decimal` amount, ISO 4217 currency whitelist |
| Fraud Detector | `agents/fraud_detector.py` | Additive scoring: amount thresholds, off-hours UTC, cross-border flag → LOW / MEDIUM / HIGH |
| Settlement Processor | `agents/settlement_processor.py` | HIGH → held, MEDIUM → settled with monitoring, LOW → settled; rejected transactions pass through unchanged |
| Integrator | `integrator.py` | Creates `shared/` dirs, loads transactions, runs agents in order, writes results |
| MCP Server | `mcp/server.py` | FastMCP server: `get_transaction_status`, `list_pipeline_results`, `pipeline://summary` resource |

---

## 🛠️ Tech Stack

| Component | Choice |
|---|---|
| Language | Python 3.14 |
| Package manager | uv |
| Decimal arithmetic | `decimal.Decimal` + `ROUND_HALF_UP` |
| Currency whitelist | ISO 4217: USD, EUR, GBP, JPY, CHF |
| MCP server | FastMCP ≥ 3.1.1 |
| Testing | pytest + pytest-cov (97% coverage) |
| Coverage gate | VS Code PreToolUse hook (blocks push < 80%) |

---

## 📂 Project Structure

```
homework-6/
├── agents/
│   ├── transaction_validator.py
│   ├── fraud_detector.py
│   └── settlement_processor.py
├── mcp/
│   └── server.py
├── tests/
│   ├── conftest.py
│   ├── test_transaction_validator.py
│   ├── test_fraud_detector.py
│   ├── test_settlement_processor.py
│   ├── test_integrator.py
│   └── test_mcp_server.py
├── commands/
│   ├── run-pipeline.md
│   ├── validate-transactions.md
│   └── write-spec.md
├── hooks/
│   └── coverage-gate.json
├── scripts/
│   └── check-coverage.sh
├── shared/              ← created at runtime
│   ├── input/
│   ├── processing/
│   ├── output/
│   └── results/
├── integrator.py
├── mcp.json
├── pyproject.toml
├── sample-transactions.json
├── specification.md
├── agents.md
└── research-notes.md
```

---

<div align="center">

*Created by Serhii Musiienko as part of the AI-Assisted Development course.*

</div>
