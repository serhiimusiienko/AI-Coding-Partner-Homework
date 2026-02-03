# Architecture

## High-Level Design
```mermaid
flowchart LR
  Client --> API[Controllers]
  API --> Svc[TicketService]
  Svc --> Repo[In-memory Repository]
  API --> Import[TicketImportService]
  API --> Classifier[TicketClassifier]
```

## Components
- Controllers: expose REST endpoints, validation and error handling.
- TicketService: business logic, timestamps, updates.
- TicketRepository: thread-safe in-memory store (ConcurrentHashMap).
- TicketImportService: parse CSV/JSON/XML, validate and persist, summarize results.
- TicketClassifier: rule-based keyword matching with confidence and decision log.

## Data Flow (Import)
```mermaid
sequenceDiagram
  participant C as Client
  participant API as Controller
  participant IMP as ImportService
  participant SVC as TicketService
  participant REP as Repository
  C->>API: POST /tickets/import (multipart)
  API->>IMP: import(file)
  IMP->>IMP: parse + validate records
  IMP->>SVC: create(valid tickets)
  SVC->>REP: save
  IMP-->>API: ImportSummary
  API-->>C: 200 OK
```

## Decisions & Trade-offs
- In-memory storage: simplicity over persistence; suitable for homework.
- Rule-based classifier: deterministic and testable; ML could be added later.
- Snake_case JSON: improves alignment with CSV headers and clarity.

## Security & Performance
- Validate inputs; strict deserialization (`fail-on-unknown-properties`).
- Concurrency: repository is thread-safe; tests planned for 20+ concurrent ops.
- Performance: basic load via parallel requests; optional Gatling scenario.
