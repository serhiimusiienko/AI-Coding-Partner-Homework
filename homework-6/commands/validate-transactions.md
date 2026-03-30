---
mode: 'agent'
description: 'Validate sample transactions in dry-run mode and print a summary table.'
---

Validate all transactions in sample-transactions.json without processing them.

Steps:
1. Run the validator in dry-run mode (e.g. uv run python agents/transaction_validator.py --dry-run)
2. Report: total count, valid count, invalid count, reasons for rejection
3. Show a table of results
