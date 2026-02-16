# GitHub Copilot Instructions — Virtual Card Management

## General
- Use Java 17+ features
- Prefer immutability
- Constructor injection only
- One class = one responsibility
- Never expose entity objects in API responses

## Naming
- DTOs: `CreateCardRequest`, `CardResponse`, `UpdateLimitsRequest`
- Services: `CardService`, `TransactionService`, `AuditService`
- Repositories: `CardRepository`, `TransactionRepository`
- Methods: verb-noun (`freezeCard`, `getHistory`, `logAudit`)
- Constants: `UPPER_SNAKE_CASE`
- Variables: `camelCase`
- Classes: `PascalCase`

## Money & Numbers
- **Always** use `BigDecimal` for money
- **Never** use `float` or `double` for money
- Round with `RoundingMode.HALF_UP`
- Store currency as ISO 4217 String (`USD`, `EUR`)
- Compare money with `.compareTo()`, not `.equals()`
- Scale = 2 decimal places for all currency amounts

## Security — Card Data
- **Never** store PAN in plain text
- Store only masked PAN (`**** **** **** 1234`)
- Store tokenized reference, not actual card number
- **Never** log PAN
- **Never** return PAN in API response
- **Never** include PAN in error messages or stack traces
- Encrypt tokens at rest

## Security — General
- Validate all inputs at API boundary
- Use Bean Validation annotations (`@NotNull`, `@Size`, `@Positive`)
- Reject unexpected fields
- Require authentication on all endpoints
- Scope cardholder endpoints to authenticated user
- Require `ROLE_OPS` for ops endpoints
- **Never** hardcode secrets or credentials
- Use environment variables for secrets
- Map domain exceptions to proper HTTP status codes
- Generic error messages only (no stack traces to clients)

## DTOs
- Use Java `record` for all request/response DTOs
- Add Bean Validation on DTO fields
- Never expose entity classes in API layer
- DTOs are immutable by default

## Controllers
- Keep controllers thin — delegate to services
- Use `@Valid` on all request bodies
- Return appropriate HTTP status codes (200, 201, 204, 400, 403, 404, 409, 422)
- Use consistent error response structure: `{ "code": "...", "message": "..." }`
- **No business logic in controllers**

## Services
- All business logic in service layer
- Validate business rules before persistence
- Use transactions for multi-step operations
- Log all state changes via `AuditService`
- Throw domain exceptions for business rule violations

## Entities
- Use `@Entity`, `@Table`, `@Column`
- UUID primary keys
- Add `createdAt` and `updatedAt` timestamps
- Use enums for status fields (`ACTIVE`, `FROZEN`, `CLOSED`)
- Add Bean Validation annotations
- Never expose entities in API responses

## Repositories
- Extend `JpaRepository<Entity, UUID>`
- Use Spring Data method naming (`findByUserId`, `findByCardToken`)
- Add `@Query` only when method names are insufficient
- Return `Optional` for single results

## Audit Logging
- Log every state change (create, update, delete, freeze, unfreeze, close)
- Include: entity type, entity ID, action, actor ID, timestamp, old value, new value
- Store old/new values as JSON strings
- Audit log table is append-only (immutable)
- Never throw exceptions from audit logging — log and swallow

## Testing — Unit Tests
- Test every public service method
- Use JUnit 5 + Mockito
- Mock all dependencies with `@Mock` or `@MockBean`
- Test happy path + error cases
- Test edge cases (frozen card, limit exceeded, invalid input)
- Use AssertJ `assertThat()` style
- Minimum 80% line coverage

## Testing — Integration Tests
- Test every REST endpoint with `@WebMvcTest` + `MockMvc`
- Test HTTP status codes and response structure
- Test validation errors return 400 with field details
- Test authorization (401/403 for unauthorized)
- Verify PAN never appears in any response
- Use fixtures from `src/test/resources/fixtures/`

## Testing — Fixtures
- Store test data in JSON/YAML files
- Use realistic but fake data
- Include valid and invalid test cases
- Do not use production data

## Error Handling
- Create domain exceptions: `CardNotFoundException`, `CardFrozenException`, `LimitExceededException`
- Use `@RestControllerAdvice` for global exception handling
- Map exceptions to HTTP status codes consistently
- Return `ErrorResponse` DTO with code and message
- Never leak stack traces or internal details to clients

## Forbidden
- ❌ `float` or `double` for money
- ❌ Storing or logging PAN in plain text
- ❌ Business logic in controllers
- ❌ Exposing entity objects in API responses
- ❌ Hardcoded secrets, keys, or credentials
- ❌ Missing input validation
- ❌ Relying on client-side validation alone
- ❌ Using `.equals()` to compare `BigDecimal` amounts
- ❌ Mutable DTOs
- ❌ Field injection (use constructor injection)
- ❌ Generic catch-all exceptions without proper handling
- ❌ Skipping tests for "simple" methods

## Patterns to Use
- ✅ Constructor injection
- ✅ Java `record` for DTOs
- ✅ `Optional` for nullable returns
- ✅ Bean Validation at API boundary
- ✅ Centralized exception handling
- ✅ Immutable objects where possible
- ✅ SOLID principles
- ✅ Consistent error response format
- ✅ Audit trail for all mutations
- ✅ Scoped authorization (user can only access their own data)

## Code Style
- Max line length: 120 characters
- Use meaningful variable names
- No abbreviations (except standard: `id`, `dto`, `uuid`)
- One statement per line
- Avoid nested ternary operators
- Keep methods short (max 20 lines ideally)
- Document complex business logic with comments
- Use `@NotNull`, `@Nullable` annotations where helpful

## Dependencies
- Use Gradle for builds
- Spring Boot Starter dependencies
- Spring Data JPA
- Spring Security
- Bean Validation (Jakarta)
- JUnit 5, Mockito, AssertJ for testing
- H2 for dev/test database
- No unnecessary third-party libraries

## Performance
- Use pagination for list endpoints (`Pageable`)
- Add database indexes on frequently queried fields
- Avoid N+1 queries (use `@EntityGraph` or JOIN FETCH)
- Cache only when needed and measured

## Documentation
- JavaDoc for public APIs
- Document business rules and constraints
- Include examples in JavaDoc
- Keep docs up to date with code changes
