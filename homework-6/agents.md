# Agents — Banking Pipeline

Four meta-agents build and verify the transaction processing system. Each has a single job.

---

## Agent 1 — Specification Writer

**Role**: Produces `specification.md` before any code is written.

| | |
|---|---|
| **Input** | `specification-TEMPLATE-hint.md`, `sample-transactions.json` |
| **Output** | `specification.md` (5-section spec), updated `agents.md` |

**Rules**:
1. Follow the 5-section template exactly (Objective → Mid-Level → Notes → Context → Low-Level Tasks).
2. Every Low-Level Task must include an exact AI prompt, file name, function name, and decision details.
3. Use real transaction IDs from `sample-transactions.json` as examples.

---

## Agent 2 — Code Generator

**Role**: Implements the 3-agent pipeline from the spec.

| | |
|---|---|
| **Input** | `specification.md`, context7 MCP (framework docs lookup) |
| **Output** | `integrator.py`, `agents/transaction_validator.py`, `agents/fraud_detector.py`, `agents/settlement_processor.py`, `research-notes.md` |

**Rules**:
1. Use `decimal.Decimal` for all monetary values — never `float`.
2. Query context7 at least twice and document each query in `research-notes.md`.
3. Agents communicate via JSON files in `shared/input/`, `shared/processing/`, `shared/output/`, `shared/results/`.

---

## Agent 3 — Test & Automation Agent

**Role**: Writes unit tests and wires up skills + coverage hook.

| | |
|---|---|
| **Input** | Agent source files in `agents/`, `integrator.py` |
| **Output** | `tests/` directory, `commands/run-pipeline.md`, `commands/validate-transactions.md`, coverage gate hook in settings |

**Rules**:
1. Every agent module needs unit tests + at least 1 integration test for the full pipeline.
2. Tests must isolate `shared/` I/O — use `tmp_path` or equivalent, never touch real directories.
3. Coverage gate hook must **block `git push`** if coverage is below 80%.

---

## Agent 4 — Documentation Agent

**Role**: Generates `README.md`, `HOWTORUN.md`, and screenshots guide.

| | |
|---|---|
| **Input** | All source files, test results, pipeline run output |
| **Output** | `README.md`, `HOWTORUN.md`, `docs/screenshots/` (5 screenshots) |

**Rules**:
1. `README.md` must include the author's name, an ASCII pipeline diagram, and a tech-stack table.
2. `HOWTORUN.md` must have numbered steps from `uv sync` to a full demo run.
3. PR description must embed all 5 screenshots so reviewers see everything without cloning.