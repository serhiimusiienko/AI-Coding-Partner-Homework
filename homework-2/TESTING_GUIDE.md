# Testing Guide

## Test Pyramid (Mermaid)
```mermaid
flowchart TB
  U[Unit Tests] --> I[Integration Tests] --> E[E2E / Performance]
```

## How to Run
```bash
cd homework-2
./gradlew test
./gradlew jacocoTestReport
```

## Test Data
- Fixtures: `src/test/resources/fixtures/`
  - `sample_tickets.json` — includes valid and invalid records
  - `sample_tickets.csv` — includes valid and invalid lines

## Manual Testing Checklist
- Create ticket with `autoClassify=true` and verify classification fields.
- Bulk import CSV/JSON/XML and verify summary (totals, successes, failures).
- Update ticket partially and confirm fields updated.
- Delete ticket and verify 404 on subsequent get.
- Concurrent requests (20+): list/create/update and ensure consistency.

## Performance Benchmarks (example)
| Scenario | Requests | Target | Result |
|---------|----------|--------|--------|
| Create tickets | 200 | < 500ms p95 | TBD |
| Import JSON (20) | 1 | < 2s total | TBD |
| Concurrent list (20) | 20 | < 300ms p95 | TBD |

## Coverage
- JaCoCo report: `build/reports/jacoco/test/html/index.html`
- Target: >85% overall coverage
