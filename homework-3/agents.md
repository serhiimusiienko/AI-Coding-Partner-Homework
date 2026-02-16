# Agent Guidelines — Virtual Card Management

## Tech Stack
- **Build**: Gradle (abstract, vendor-agnostic config)
- **Language**: Java 17+
- **Framework**: Spring Boot (Data JPA, Security, Validation)
- **Database**: Relational (abstract; H2 for dev/test, Postgres/MySQL for prod)
- **Testing**: JUnit 5, Mockito, Spring MockMvc

## Domain Rules (Banking/PCI DSS)
- **Never** store card PAN in plain text — masked format only (`**** **** **** 1234`) + token reference
- **All money fields**: `BigDecimal` with `RoundingMode.HALF_UP`, 2 decimal places; never `float`/`double`
- **Currency**: ISO 4217 codes (3-char String: `USD`, `EUR`, etc.)
- **No PAN** in logs, API responses, error messages, or stack traces
- **Audit every state change**: immutable log entries (create, freeze, limit update, close)
- **Cardholder data scoped**: a user can only access/modify their own cards

## Code Style
- **DTOs**: Use Java `record` types for request/response; never expose entities in API layer
- **Naming**: `CardService.freezeCard(...)`, `CardRepository.findByUserId(...)` — verb-noun, self-documenting
- **SOLID**: Single responsibility per class; inject dependencies via constructor
- **Immutability**: DTOs and value objects immutable by default
- **Validation**: Use Bean Validation (`@NotNull`, `@Size`, `@Positive`) on DTOs; fail fast at API boundary
- **Error handling**: Domain exceptions map to HTTP status codes; centralized `@RestControllerAdvice`

## Testing
- **Unit tests**: Every service method tested with mocked dependencies (JUnit 5 + Mockito)
- **Integration tests**: Every REST endpoint tested with `@WebMvcTest` + `MockMvc`
- **Fixtures**: JSON/YAML test data in `src/test/resources/fixtures/`
- **Coverage**: Minimum 80% line coverage
- **Test both**: Happy path + error cases (frozen card, limit exceeded, invalid input, unauthorized access)
- **Assertions**: Use AssertJ `assertThat()` style; avoid `assertTrue(x == y)`

## Security
- **Authentication**: All endpoints require authentication; use Spring Security
- **Authorization**: Cardholder endpoints scoped to authenticated user; ops endpoints require `ROLE_OPS`
- **Input validation**: Reject unexpected fields, validate all inputs at API boundary
- **Audit log**: Every mutation logged with actor, timestamp, old/new values to immutable table
- **Secrets**: Never hardcode secrets; use env vars or secret management service
- **Encryption**: PAN tokens encrypted at rest; TLS/HTTPS for transit

## What to Avoid
- ❌ `float` or `double` for money — always `BigDecimal`
- ❌ Storing PAN in plain text or logs
- ❌ Business logic in controllers — keep controllers thin, delegate to services
- ❌ Exposing entity objects in API responses — use DTOs
- ❌ Hardcoded secrets, keys, or credentials
- ❌ Missing input validation or relying on client-side validation alone
- ❌ Generic error messages that leak implementation details
