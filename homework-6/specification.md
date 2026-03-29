# Specification: AI-Powered Multi-Agent Banking Pipeline

## 1. High-Level Objective

Build a 3-agent Python pipeline that validates, scores for fraud risk, and settles banking transactions using file-based JSON message passing.

---

## 2. Mid-Level Objectives

- Transactions with missing required fields or invalid data are rejected with a machine-readable `reason` (e.g. `INVALID_CURRENCY` for TXN006/XYZ, `INVALID_AMOUNT` for TXN007/-100.00)
- Transactions above $10,000 receive `fraud_risk: "HIGH"` — TXN005 ($75,000) must score HIGH
- All monetary values are handled with `decimal.Decimal` — never `float`
- Every agent logs each operation with ISO 8601 timestamp, agent name, transaction ID, and outcome; account numbers are masked to last 4 chars
- All 8 sample transactions produce a result file in `shared/results/` after a full pipeline run
- Test coverage gate blocks `git push` if coverage falls below 80%; target ≥ 90%

---

## 3. Implementation Notes

- **Monetary values**: `decimal.Decimal` only — import from Python `decimal` module, use `ROUND_HALF_UP`
- **Currency whitelist**: ISO 4217 — `USD`, `EUR`, `GBP`, `JPY`, `CHF` only
- **Logging**: every agent call writes one audit line: `{timestamp} | {agent} | {txn_id} | {outcome}`
- **PII masking**: log account numbers as `****{last4}` — e.g. `ACC-1006` → `****1006`
- **Error handling**: catch all exceptions per message; write a `rejected` result rather than crashing the pipeline
- **Message format**: standard JSON envelope (see Task 2 in TASKS.md); `data.amount` stored as string to preserve decimal precision

---

## 4. Context

- **Beginning state**: `sample-transactions.json` exists with 8 raw transaction records. No agents exist. No `shared/` directories exist.
- **Ending state**: All 8 transactions processed. Results written to `shared/results/`. Test coverage ≥ 90%. `README.md` and `HOWTORUN.md` complete.

---

## 5. Low-Level Tasks

### Task: Transaction Validator

**Prompt**:
```
Context: Python 3.14 project using decimal.Decimal. shared/ dirs will be created by the integrator.
Task: Create agents/transaction_validator.py with a process_message(message: dict) -> dict function.
Rules:
  - Required fields: transaction_id, amount, currency, source_account, destination_account
  - amount must be a positive decimal.Decimal (reject negatives and zero) → reason: INVALID_AMOUNT
  - currency must be in whitelist [USD, EUR, GBP, JPY, CHF] → reason: INVALID_CURRENCY
  - Missing any required field → reason: MISSING_FIELD
  - Log every transaction with masked account numbers (last 4 chars only)
  - Return message dict with data.status = "validated" | "rejected" and data.reason on rejection
Examples:
  - TXN006 currency=XYZ → rejected, reason=INVALID_CURRENCY
  - TXN007 amount=-100.00 → rejected, reason=INVALID_AMOUNT
Output: single Python file, no external deps beyond stdlib
```

**File to CREATE**: `agents/transaction_validator.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Check required fields: `transaction_id`, `amount`, `currency`, `source_account`, `destination_account`
- Parse `amount` as `decimal.Decimal`; reject if ≤ 0
- Reject unknown currency codes (whitelist: `USD`, `EUR`, `GBP`, `JPY`, `CHF`)
- Return the original message dict with `data.status` set to `"validated"` or `"rejected"`, plus `data.reason` on failure

---

### Task: Fraud Detector

**Prompt**:
```
Context: Python 3.14 project. Receives validated messages from Transaction Validator via shared/output/.
Task: Create agents/fraud_detector.py with a process_message(message: dict) -> dict function.
Rules:
  - Only process messages where data.status == "validated"
  - Score transactions on a 0–10 scale using these additive triggers:
      amount > $10,000  → +3 pts
      amount > $50,000  → +4 pts additional (total +7 for >$50k)
      hour in 02–05 UTC → +2 pts  (unusual hour)
      metadata.country != source account's country → +1 pt  (cross-border proxy)
  - Risk levels: LOW (0–2), MEDIUM (3–6), HIGH (7–10)
  - Add fraud_risk_score (int) and fraud_risk_level (str) to message data
  - Log outcome with masked account numbers
Examples:
  - TXN005 amount=75000.00 → score ≥ 7, fraud_risk_level=HIGH
  - TXN004 amount=500.00, hour=02 UTC → score = 2, fraud_risk_level=LOW
Output: single Python file, no external deps beyond stdlib
```

**File to CREATE**: `agents/fraud_detector.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Skip (pass through) messages with `data.status == "rejected"`
- Parse `timestamp` to extract UTC hour for unusual-hour check
- Apply additive scoring rules above; cap at 10
- Attach `data.fraud_risk_score` and `data.fraud_risk_level` before returning

---

### Task: Settlement Processor

**Prompt**:
```
Context: Python 3.14 project. Receives fraud-scored messages from Fraud Detector.
Task: Create agents/settlement_processor.py with a process_message(message: dict) -> dict function.
Rules:
  - Pass-through rejected transactions unchanged (preserve rejection reason)
  - For HIGH fraud_risk_level: set data.status = "held", data.settlement_note = "Held for manual review"
  - For MEDIUM fraud_risk_level: set data.status = "settled", data.settlement_note = "Settled with enhanced monitoring"
  - For LOW fraud_risk_level: set data.status = "settled", data.settlement_note = "Settled"
  - Round final amount to 2 decimal places using decimal.Decimal ROUND_HALF_UP
  - Log every decision with masked account numbers and final status
Output: single Python file, no external deps beyond stdlib
```

**File to CREATE**: `agents/settlement_processor.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Receives messages after fraud scoring; reads `data.fraud_risk_level`
- `HIGH` → status `"held"` (no money movement, queued for review)
- `MEDIUM` → status `"settled"` with a monitoring flag
- `LOW` → status `"settled"` cleanly
- Always re-serialize `amount` as string with 2 decimal places before writing result
