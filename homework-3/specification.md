# Virtual Card Management — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

---

## High-Level Objective

Build a Virtual Card Management service for a banking application that allows cardholders to create virtual cards, freeze/unfreeze them, set spending limits, and view transaction history — while maintaining full audit trails and PCI DSS compliance.

---

## Mid-Level Objectives

- **Card lifecycle API**: Expose REST endpoints for creating, retrieving, freezing/unfreezing, and closing virtual cards.
- **Spending limits**: Allow cardholders to set and update per-transaction and daily spending limits; enforce them during transaction authorization.
- **Transaction history**: Provide paginated, filterable transaction history per card with support for date range and status filters.
- **Audit & compliance**: Log every state change (create, freeze, limit update) to an immutable audit log table; include actor, timestamp, old/new values.
- **Ops dashboard data**: Expose internal endpoints for the compliance/ops team to query cards by status, flag suspicious activity, and export audit logs.

---

## Implementation Notes

### Tech Stack
- Java 17+, Spring Boot, Gradle
- Spring Data JPA (relational DB — abstract the vendor, use H2 for dev/test)
- Spring Security for endpoint protection
- Bean Validation (Jakarta) for input constraints

### Money Handling
- All monetary fields use `BigDecimal` with `RoundingMode.HALF_UP` and 2 decimal places.
- Currency stored as ISO 4217 code (`String`, 3 chars). Never use `float`/`double` for money.

### Security & PCI DSS
- Card numbers (PAN) are **never** stored in plain text. Store only a masked version (`**** **** **** 1234`) and a tokenized reference.
- No PAN in logs, API responses, or error messages.
- All endpoints require authentication. Cardholder endpoints scoped to the authenticated user; ops endpoints require `ROLE_OPS`.
- Input validation on every request. Reject unexpected fields.

### Testing
- Unit tests for every service method (JUnit 5 + Mockito).
- Integration tests for each REST endpoint (Spring `MockMvc`).
- Minimum 80% line coverage.
- Test edge cases: frozen card transactions, limit exceeded, invalid inputs.

### Error Handling
- Use a consistent error response DTO: `{ "code": "CARD_FROZEN", "message": "..." }`.
- Map domain exceptions to appropriate HTTP status codes (400, 403, 404, 409, 422).

---

## Context

### Beginning Context
These files/resources exist before implementation starts:
- `build.gradle` — project skeleton with Spring Boot, JPA, Security, Validation, JUnit 5 dependencies
- `src/main/resources/application.yml` — datasource, JPA, and security base config
- `src/main/java/.../VirtualCardApplication.java` — Spring Boot main class
- `src/test/resources/fixtures/sample_cards.json` — seed data for tests
- `src/test/resources/fixtures/sample_transactions.json` — seed transaction data for tests
- `README.md` — project overview

### Ending Context
These files will exist after all tasks are complete:
- `src/main/java/.../domain/VirtualCard.java` — card entity
- `src/main/java/.../domain/Transaction.java` — transaction entity
- `src/main/java/.../domain/AuditLogEntry.java` — audit log entity
- `src/main/java/.../api/CardController.java` — cardholder REST endpoints
- `src/main/java/.../api/OpsController.java` — ops/compliance REST endpoints
- `src/main/java/.../api/dto/*.java` — request/response DTOs
- `src/main/java/.../service/CardService.java` — card business logic
- `src/main/java/.../service/TransactionService.java` — transaction query logic
- `src/main/java/.../service/AuditService.java` — audit logging
- `src/main/java/.../repository/CardRepository.java` — card data access
- `src/main/java/.../repository/TransactionRepository.java` — transaction data access
- `src/main/java/.../repository/AuditLogRepository.java` — audit log data access
- `src/main/java/.../exception/GlobalExceptionHandler.java` — centralized error handling
- `src/test/java/.../service/CardServiceTest.java` — unit tests
- `src/test/java/.../api/CardControllerTest.java` — integration tests

---

## Low-Level Tasks

### Task 1 — Domain Entities

**Prompt:** Create the JPA entity classes for virtual cards, transactions, and audit log entries.

**Files to CREATE:**
- `src/main/java/.../domain/VirtualCard.java`
- `src/main/java/.../domain/Transaction.java`
- `src/main/java/.../domain/AuditLogEntry.java`

**Functions/Classes:** `VirtualCard`, `Transaction`, `AuditLogEntry`

**Details:**
- `VirtualCard`: fields — `id` (UUID), `maskedPan` (String, last 4 digits only), `cardToken` (String, unique), `status` (enum: ACTIVE, FROZEN, CLOSED), `perTransactionLimit` (BigDecimal), `dailyLimit` (BigDecimal), `currency` (String, 3 chars), `userId` (UUID), `createdAt`, `updatedAt`.
- `Transaction`: fields — `id` (UUID), `cardId` (FK), `amount` (BigDecimal), `currency`, `merchantName`, `status` (enum: APPROVED, DECLINED), `createdAt`.
- `AuditLogEntry`: fields — `id` (UUID), `entityType`, `entityId`, `action` (String), `actorId` (UUID), `oldValue` (JSON String), `newValue` (JSON String), `timestamp`.
- Use `@Entity`, `@Table`, `@Column` annotations. Add Bean Validation annotations (`@NotNull`, `@Size`, etc.).

---

### Task 2 — Repositories

**Prompt:** Create Spring Data JPA repository interfaces for all three entities.

**Files to CREATE:**
- `src/main/java/.../repository/CardRepository.java`
- `src/main/java/.../repository/TransactionRepository.java`
- `src/main/java/.../repository/AuditLogRepository.java`

**Functions/Classes:** `CardRepository`, `TransactionRepository`, `AuditLogRepository`

**Details:**
- `CardRepository`: `findByUserId(UUID)`, `findByCardTokenAndUserId(String, UUID)`, `findByStatus(CardStatus)`.
- `TransactionRepository`: `findByCardIdOrderByCreatedAtDesc(UUID, Pageable)`, `findByCardIdAndCreatedAtBetween(UUID, Instant, Instant)`, query to sum daily spending: `sumAmountByCardIdAndCreatedAtAfter(UUID, Instant)`.
- `AuditLogRepository`: `findByEntityTypeAndEntityId(String, UUID)`.
- Use `JpaRepository<Entity, UUID>`. Add `@Query` only where Spring Data method names are insufficient.

---

### Task 3 — Services (Business Logic)

**Prompt:** Implement the card management service with spending limit enforcement, and the audit logging service.

**Files to CREATE:**
- `src/main/java/.../service/CardService.java`
- `src/main/java/.../service/TransactionService.java`
- `src/main/java/.../service/AuditService.java`

**Functions/Classes:** `CardService`, `TransactionService`, `AuditService`

**Details:**
- `CardService.createCard(userId, request)` — generates token, sets defaults, persists, logs audit entry. Return masked PAN only.
- `CardService.freezeCard(userId, cardToken)` / `unfreezeCard(...)` — validates ownership, checks current status, updates, logs audit.
- `CardService.updateLimits(userId, cardToken, newLimits)` — validate limits > 0, update, log old/new values.
- `CardService.closeCard(userId, cardToken)` — terminal state, cannot be reversed.
- `TransactionService.authorizeTransaction(cardToken, amount)` — check card is ACTIVE, amount ≤ per-transaction limit, daily sum + amount ≤ daily limit. Return APPROVED/DECLINED.
- `TransactionService.getHistory(userId, cardToken, filters, pageable)` — validate ownership, return paginated results.
- `AuditService.log(entityType, entityId, action, actorId, oldValue, newValue)` — persist entry. No exceptions may escape this method (log and swallow).
- All money comparisons use `BigDecimal.compareTo()`, never `equals()`.

---

### Task 4 — REST Controllers & DTOs

**Prompt:** Create the REST API layer with request/response DTOs, cardholder endpoints, and ops/compliance endpoints.

**Files to CREATE:**
- `src/main/java/.../api/CardController.java`
- `src/main/java/.../api/OpsController.java`
- `src/main/java/.../api/dto/CreateCardRequest.java`
- `src/main/java/.../api/dto/UpdateLimitsRequest.java`
- `src/main/java/.../api/dto/CardResponse.java`
- `src/main/java/.../api/dto/TransactionResponse.java`
- `src/main/java/.../api/dto/ErrorResponse.java`

**Functions/Classes:** `CardController`, `OpsController`, DTO records

**Details:**
- Cardholder endpoints (all scoped to authenticated user):
  - `POST /api/cards` — create card. Returns 201 + `CardResponse`.
  - `GET /api/cards` — list user's cards.
  - `PATCH /api/cards/{token}/freeze` — freeze. Returns 200.
  - `PATCH /api/cards/{token}/unfreeze` — unfreeze. Returns 200.
  - `PUT /api/cards/{token}/limits` — update limits. Body: `UpdateLimitsRequest`. Returns 200.
  - `DELETE /api/cards/{token}` — close card. Returns 204.
  - `GET /api/cards/{token}/transactions?from=&to=&page=&size=` — paginated history.
- Ops endpoints (require `ROLE_OPS`):
  - `GET /api/ops/cards?status=` — list cards by status.
  - `GET /api/ops/audit/{entityId}` — audit trail for an entity.
- Use `@Valid` on all request bodies. DTOs as Java `record` types. Never expose entity objects directly.

---

### Task 5 — Exception Handling

**Prompt:** Create domain exceptions and a global exception handler that returns consistent error responses.

**Files to CREATE:**
- `src/main/java/.../exception/CardNotFoundException.java`
- `src/main/java/.../exception/CardFrozenException.java`
- `src/main/java/.../exception/LimitExceededException.java`
- `src/main/java/.../exception/InvalidCardStateException.java`
- `src/main/java/.../exception/GlobalExceptionHandler.java`

**Functions/Classes:** Exception classes, `GlobalExceptionHandler`

**Details:**
- Each exception extends `RuntimeException` with a domain error code (String constant).
- `GlobalExceptionHandler` uses `@RestControllerAdvice`. Maps:
  - `CardNotFoundException` → 404
  - `CardFrozenException` → 409
  - `LimitExceededException` → 422
  - `InvalidCardStateException` → 409
  - `MethodArgumentNotValidException` → 400 (list field errors)
  - `AccessDeniedException` → 403
  - Catch-all `Exception` → 500 (generic message, never leak internals).
- Response body always uses `ErrorResponse` DTO: `{ "code": "...", "message": "..." }`.

---

### Task 6 — Tests

**Prompt:** Write unit tests for CardService and integration tests for CardController.

**Files to CREATE:**
- `src/test/java/.../service/CardServiceTest.java`
- `src/test/java/.../api/CardControllerTest.java`

**Functions/Classes:** `CardServiceTest`, `CardControllerTest`

**Details:**
- `CardServiceTest` (JUnit 5 + Mockito):
  - Test `createCard` — verify card persisted, audit logged, masked PAN returned.
  - Test `freezeCard` — verify status change, audit entry, rejection if already frozen.
  - Test `updateLimits` — verify new limits saved, rejects zero/negative values.
  - Test `closeCard` — verify terminal state, cannot freeze after close.
  - Test `authorizeTransaction` — approve within limits, decline when frozen, decline when over limit.
- `CardControllerTest` (MockMvc + `@WebMvcTest`):
  - Test each endpoint returns correct HTTP status and body structure.
  - Test validation errors return 400 with field-level messages.
  - Test unauthorized access returns 401/403.
  - Test that PAN never appears in any response body or header.
- Use `@MockBean` for service dependencies in controller tests. Use `assertThat` (AssertJ) style assertions.
