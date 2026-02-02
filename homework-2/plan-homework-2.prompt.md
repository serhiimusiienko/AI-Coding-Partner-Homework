# Action Execution Plan — Homework 2

Role: Senior Java Developer & Senior AI Vibe-Coding Engineer

## Context & Objectives
Build an Intelligent Customer Support System that:
- Imports tickets from CSV/JSON/XML, validates inputs, and reports import summaries.
- Exposes a REST API for CRUD operations and filtering.
- Auto-classifies tickets (category, priority, confidence, reasoning, keywords).
- Achieves >85% test coverage with AI-assisted tests.
- Produces multi-level documentation with Mermaid diagrams.

This plan aligns the project to Java 21, Spring Boot, Gradle, and JUnit with automated documentation and AI assistance throughout.

## Tech Stack & Tooling
- Java: 21 (JDK 21)
- Framework: Spring Boot (REST + validation)
- Build: Gradle (Java, Spring Boot, JaCoCo plugins)
- Testing: JUnit (latest Jupiter), Mockito, Testcontainers (optional for integration), Gatling/JMH (optional for perf) — see clarifications
- Parsing: Jackson (JSON + XML), Apache Commons CSV (CSV)
- Validation: Jakarta Bean Validation (email, size, enum constraints)
- Coverage: JaCoCo (HTML/XML reports)
- Docs: Markdown files with Mermaid diagrams; automated generation via AI assistance
- Scripts: `demo/requests.sh` for sample API requests

## Deliverables & Result Placement
All implementation results will be stored in `homework-2/`:
- Source code under `src/main/java` and `src/main/resources`
- Tests under `src/test/java` and fixtures under `src/test/resources/fixtures`
- Documentation: `README.md`, `API_REFERENCE.md`, `ARCHITECTURE.md`, `TESTING_GUIDE.md`
- Demo: `demo/requests.sh`, sample data files
- Coverage report: JaCoCo output; screenshot `docs/screenshots/test_coverage.png`

## Strict AI Rules (verbatim)
- “If you are unsure of the answer, state that you do not know. Do not guess.”
- “If the input data is ambiguous, ask 3 clarifying questions before proceeding.”
- “If data is missing from the provided text, explicitly list what is missing.”

## Clarifications (to ask before implementation)
1) JUnit 6+: JUnit 6 is not a current stable release; confirm if “6+” means “latest JUnit Jupiter (5.x)” or another framework.
2) Persistence: Should we use in-memory storage, H2, Postgres, or another DB? Requirements do not specify persistence; default will be in-memory unless directed.
3) Performance scope: For concurrency/performance tests, confirm target throughput and tools (e.g., use Spring Boot test + simple load harness vs. Gatling/JMH). Provide acceptance thresholds if any.

## Confirmed Choices
- Testing: Latest JUnit Jupiter (5.x) with Mockito and JaCoCo.
- Persistence: In-memory repository (optional H2 profile can be added later if required).
- Performance: Basic concurrency tests in Spring Boot; optional Gatling for 20+ concurrent requests.

## Step-by-Step Actions

### Phase 0 — Planning & Project Setup
1. Confirm clarifications and constraints; finalize scope to Java 21 + Spring Boot + Gradle.
2. Initialize Gradle project: Java 21 compatibility, Spring Boot plugin, dependencies (web, validation, Jackson, Commons CSV, Lombok optional), test dependencies (JUnit Jupiter, Mockito), JaCoCo.
3. Establish package structure: `tickets` domain, `api` controllers, `service`, `repository`, `importer`, `classification`, `config`, `util`.
4. Add baseline configs: `application.yml` (logging levels, serialization), Gradle `jacocoTestReport` task.

### Phase 1 — Domain Model & Validation
5. Define `Ticket` entity/DTO schema per requirements (UUID id, customer fields, subject/description, category, priority, status, timestamps, assigned_to, tags, metadata).
6. Implement Bean Validation: email format, string length bounds, enum validations; custom validators if needed.
7. Create mapping between entity and DTOs (request/response), plus error model for validation feedback.

### Phase 2 — Repository & Persistence
8. Implement in-memory repository (Map/ConcurrentMap) with CRUD and filtering. Optionally add H2 repository behind a profile.
9. Ensure thread-safety for concurrent operations; design simple index/filtering utilities.

### Phase 3 — REST API Endpoints
10. Implement endpoints:
    - `POST /tickets` (create; optional auto-classify flag)
    - `POST /tickets/import` (bulk import: CSV/JSON/XML)
    - `GET /tickets` (list with filtering by category, priority, status, tags, date ranges)
    - `GET /tickets/{id}` (get)
    - `PUT /tickets/{id}` (update)
    - `DELETE /tickets/{id}` (delete)
11. Standardize responses with appropriate HTTP status codes and error handling (400, 404, 201, 200, 204).
12. Implement filtering and pagination parameters; validate and sanitize inputs.

### Phase 4 — Importers & Bulk Summary
13. CSV importer with Apache Commons CSV; robust header & delimiter handling.
14. JSON importer with Jackson; support array payloads.
15. XML importer with Jackson XML; define compatible schema.
16. Validate records per model; accumulate summary: total, successes, failures with error details (line/index + reason).
17. Malformed files/error handling: detailed messages, partial success allowed; transactional behavior documented.

### Phase 5 — Auto-Classification
18. Rule-based classifier: keyword detection for categories and priority; confidence scoring based on keyword weights.
19. Implement `POST /tickets/{id}/auto-classify`: returns category, priority, confidence, reasoning, keywords.
20. Auto-run classification on ticket creation when flag is present; store confidence; allow overrides.
21. Log decisions to an audit trail (in-memory log or simple store) for traceability.

### Phase 6 — Demo Script & Sample Data
22. Create `demo/requests.sh` with cURL commands for all endpoints (create, list/filter, get/update/delete, import for each format, auto-classify).
23. Prepare sample data: `sample_tickets.csv` (50), `sample_tickets.json` (20), `sample_tickets.xml` (30), plus invalid files for negative tests in `src/test/resources/fixtures`.

### Phase 7 — Testing & Coverage
24. Configure JUnit Jupiter and Mockito; ensure Gradle test tasks run.
25. Implement test suites (aiming counts per spec):
    - `TicketApiTests` (~11)
    - `TicketModelValidationTests` (~9)
    - `ImportCsvTests` (~6)
    - `ImportJsonTests` (~5)
    - `ImportXmlTests` (~5)
    - `CategorizationTests` (~10)
    - `IntegrationWorkflowTests` (~5)
    - `PerformanceTests` (~5)
26. Use fixtures for sample/invalid data; verify import summaries and error details.
27. Concurrency tests: 20+ simultaneous requests; validate repository thread-safety and consistent results.
28. Achieve >85% coverage; configure JaCoCo reporting; export screenshot to `docs/screenshots/test_coverage.png`.

### Phase 8 — Documentation (Automated with AI Assistance)
29. `README.md`: overview, features, architecture diagram (Mermaid), setup, run & test instructions, structure.
30. `API_REFERENCE.md`: endpoint specs, request/response examples, schemas, error formats, cURL examples.
31. `ARCHITECTURE.md`: high-level architecture, components, data flow (Mermaid sequence diagrams), decisions, security/performance considerations.
32. `TESTING_GUIDE.md`: test pyramid (Mermaid), how to run tests, sample data locations, manual checklist, performance benchmarks.
33. Inline comments: key classes/methods explaining design choices.
34. Note usage of different AI models/tools per doc type and how this improves speed/clarity.

### Phase 9 — Verification & Packaging
35. Run full test suite; confirm coverage threshold and stability.
36. Validate demo script end-to-end; confirm all cURL samples succeed.
37. Finalize documentation; verify at least 3 Mermaid diagrams across docs.
38. Compile acceptance report mapping criteria to results.

## Checklist
- [ ] Gradle project initialized (Java 21, Spring Boot, JaCoCo)
- [ ] Dependencies added (web, validation, Jackson, Commons CSV, test libs)
- [ ] Ticket model + DTOs + validation complete
- [ ] In-memory repository implemented and thread-safe
- [ ] All REST endpoints implemented with proper status codes
- [ ] CSV/JSON/XML importers with bulk summary and error details
- [ ] Auto-classification endpoint with confidence, reasoning, keywords
- [ ] Auto-classify on create (flag) + override supported + decision logging
- [ ] `demo/requests.sh` covers all endpoints
- [ ] Sample data + invalid fixtures prepared
- [ ] Test suites implemented; concurrency tests added
- [ ] JaCoCo coverage >85% and screenshot exported
- [ ] Documentation: README, API_REFERENCE, ARCHITECTURE, TESTING_GUIDE with >=3 Mermaid diagrams
- [ ] Inline comments added in critical code paths
- [ ] Acceptance criteria validated and recorded

## Acceptance Criteria
- Tech Stack: Java 21, Spring Boot, Gradle present and used.
- Testing: JUnit-based tests achieve >85% coverage; coverage report generated; screenshot saved to `docs/screenshots/test_coverage.png`.
- API Endpoints: All specified CRUD and import endpoints implemented; filtering available; proper HTTP codes & structured errors.
- Import: CSV/JSON/XML parsers correctly validate records; bulk summary reports totals, successes, failures with detailed reasons; robust handling of malformed inputs.
- Auto-Classification: `/tickets/{id}/auto-classify` returns category, priority, confidence, reasoning, keywords; optional auto-run on create; confidence stored; overrides allowed; decisions logged.
- Demo: `demo/requests.sh` runs successfully and demonstrates all endpoints including imports and classification.
- Documentation: Four markdown documents complete, with at least 3 Mermaid diagrams across them; API examples and schemas included; testing guide covers running tests and manual checklist; architecture doc includes data flow and design decisions.
- Code Quality: Clear structure, validation, error handling, inline comments on critical logic; follows repository organization rules in the root README.
- Result Placement: All outputs stored under `homework-2/` per this plan.

## AI Assistance Documentation
- Tasks performed with AI:
  - Test generation: Drafting unit/integration/performance tests and fixtures; refining assertions and edge cases.
  - Documentation: First drafts for README, API reference, architecture, testing guide; generating Mermaid diagrams.
  - Classification heuristics: Brainstorming keyword rules and confidence scoring approach.
  - Script authoring: Building `demo/requests.sh` with comprehensive cURL samples.
- Workflow improvements:
  - Accelerated boilerplate creation and test scaffolding, reducing setup time.
  - Better coverage through AI-suggested edge cases and negative tests.
  - Clearer docs with AI-structured outlines and diagrams, improving onboarding and maintenance.
  - Faster iteration on classification logic via AI-guided rule refinement.

## Context-Model-Prompt (CMP) Application
- Context: Provide ticket schema, endpoint requirements, parsing constraints, and validation rules to the AI.
- Model: Use appropriate AI configurations for test generation vs. documentation drafting; alternate models/tools to fit each document type.
- Prompt: Craft targeted prompts (e.g., “Generate 6 CSV import tests covering malformed headers and mixed delimiters”) and iterate with feedback to refine outputs.

## Risks & Mitigations
- JUnit version ambiguity: Confirm target; default to latest JUnit Jupiter if unclarified.
- Parsing edge cases: Add robust error reporting and unit tests for malformed inputs.
- Concurrency: Use thread-safe repositories and verify with 20+ concurrent operation tests.
- Performance scope: Set realistic local load test parameters; document assumptions and results.

---

All implementation work will be placed under `homework-2/` according to this plan, with AI assistance documented and strict rules observed.
