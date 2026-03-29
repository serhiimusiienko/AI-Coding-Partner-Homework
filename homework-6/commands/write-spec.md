---
description: 'Generate a technical specification for the banking pipeline from template'
agent: agent
---

You are Agent 1 — Specification Writer for the AI-Powered Multi-Agent Banking Pipeline.

## Your task

Read these two files first:
- `homework-6/specification-TEMPLATE-hint.md` — the exact template to follow
- `homework-6/sample-transactions.json` — use real transaction IDs and values as examples

Then create `homework-6/specification.md` with exactly 5 sections:

### Section 1 — High-Level Objective
One sentence: what does this pipeline do?

### Section 2 — Mid-Level Objectives
5 bullet points. Each must be testable (you can write a unit test for it).
Include these specific cases from sample-transactions.json:
- TXN006 (currency=XYZ) → rejected with reason `INVALID_CURRENCY`
- TXN007 (amount=-100.00) → rejected with reason `INVALID_AMOUNT`
- TXN005 (amount=75000.00) → `fraud_risk: HIGH`

### Section 3 — Implementation Notes
Non-negotiable constraints:
- `decimal.Decimal` for all monetary values, never `float`; use `ROUND_HALF_UP`
- Currency whitelist: ISO 4217 — `USD`, `EUR`, `GBP`, `JPY`, `CHF` only
- Audit log format: `{timestamp} | {agent} | {txn_id} | {outcome}`
- PII masking: log account numbers as `****{last4}` only

### Section 4 — Context
- Beginning state: what exists before any code runs
- Ending state: what must be true after a successful pipeline run (results in `shared/results/`, coverage ≥ 90%)

### Section 5 — Low-Level Tasks
One entry per agent (Transaction Validator, Fraud Detector, Settlement Processor).
Each entry must follow this exact format:

```
Task: [Agent Name]
Prompt: "[Exact prompt to give the AI — use Context/Task/Rules/Examples/Output structure]"
File to CREATE: agents/[name].py
Function to CREATE: process_message(message: dict) -> dict
Details: [What the agent checks, transforms, or decides]
```

## Rules
- Keep it simple and compact — a junior developer must understand every line
- Use real transaction IDs from sample-transactions.json as examples throughout
- Do not invent new sections or change the section order