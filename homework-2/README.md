# Intelligent Customer Support System (Homework 2)

## Overview
A Spring Boot (Java 21) application for managing support tickets:
- CRUD REST API with filtering
- Bulk import from CSV/JSON/XML with validation and summary
- Auto-classification of tickets (category, priority, confidence, reasoning, keywords)
- In-memory persistence for simplicity
- Tests (JUnit Jupiter 5, Mockito, JaCoCo) and demo requests script

## Features
- Endpoints: `/tickets` CRUD, `/tickets/import`, `/tickets/{id}/auto-classify`
- Validation via Jakarta Bean Validation
- JSON snake_case mapping
- Rule-based classifier with decision log

## Architecture (Mermaid)
```mermaid
flowchart LR
  C[Client] --> API[REST Controllers]
  API --> Svc[TicketService]
  Svc --> Repo[TicketRepository (in-memory)]
  API --> Import[TicketImportService]
  API --> Classifier[TicketClassifier]
  Classifier --> Log[Decision Log]
```

## Setup & Run
- Requires Java 21 and Gradle wrapper

Commands:
```bash
cd homework-2
./gradlew bootRun
```

## Project Structure
- `src/main/java/org/example/support` — application code
- `src/test/java` — tests
- `src/test/resources/fixtures` — sample import data
- `demo/requests.sh` — cURL samples (multipart import supported)

## Testing
Run tests and coverage:
```bash
cd homework-2
./gradlew test jacocoTestReport
```
Coverage reports:
- HTML: [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)
- XML: [homework-2/build/reports/jacoco/test/jacocoTestReport.xml](homework-2/build/reports/jacoco/test/jacocoTestReport.xml)
- Summary: [homework-2/docs/screenshots/test_coverage.md](homework-2/docs/screenshots/test_coverage.md)

## Notes
- Import expects CSV headers in snake_case.
- XML expects a list of tickets (root array).
- Use `autoClassify=true` on create to classify immediately.
