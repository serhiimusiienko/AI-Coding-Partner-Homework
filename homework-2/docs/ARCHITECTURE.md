````markdown
# Architecture

```mermaid
flowchart LR
  C[Client] --> TC[TicketController]
  TC --> TS[TicketService]
  TS --> TR[TicketRepository<br/>(in-memory)]
  TC --> TI[TicketImportService]
  TC --> CL[TicketClassifier]
```

- **API layer**: REST endpoints under `/tickets`.
- **Service layer**: create/update/list/delete; sets `id/created_at/updated_at`.
- **Repository**: `ConcurrentHashMap<UUID, Ticket>` (no DB).
- **Import**: CSV/JSON/XML -> validate -> create; returns summary + failure details.
- **Classification**: keyword-based; updates `category/priority/confidence` + logs decisions.

````
