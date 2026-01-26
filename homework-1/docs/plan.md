# Homework 1 — Copilot-Only Step-by-Step Implementation Plan (Java 21, Gradle, Spring Boot)

This plan lists only practical actions: what to type into GitHub Copilot Chat, when to generate/regenerate code, when to review/take screenshots, when to run git commands, and how to iterate to a clean final solution.

---

## 0) Preparation (once)

- In VS Code, open the workspace root.
- Create a branch for the assignment:
  ```bash
  git checkout -b homework-1-submission
  ```

---

## 1) Project Scaffolding via Copilot

- In Copilot Chat:
  "Create a new Java 21 Spring Boot project inside homework-1 using Gradle (Kotlin DSL). GroupId: com.example, artifactId: banking-api, name: Banking API. Add dependencies: spring-boot-starter-web, validation, lombok, springdoc-openapi-ui. Generate Gradle wrapper, settings.gradle.kts, build.gradle.kts, Application class, and a basic health endpoint. Place sources under homework-1/src/main/java and resources under homework-1/src/main/resources."

- Accept file suggestions. If Copilot posts large snippets, use "Insert into new file" and save to the requested paths.
- Ask Copilot to generate `.gitignore`:
  "Generate a Java/Gradle `.gitignore` tailored for this project and save it at homework-1/.gitignore."
- Review build files for Java 21, plugin versions, and dependencies. If versions look outdated, in Copilot Chat:
  "Regenerate build.gradle.kts with latest stable Spring Boot and plugin versions targeting Java 21. Keep dependencies as requested."
- Take screenshot: build files and project tree in VS Code Explorer.

---

## 2) Domain Model and Storage

- In Copilot Chat:
  "Create `Transaction` model (id, fromAccount, toAccount, amount, currency, type [deposit|withdrawal|transfer], timestamp, status [pending|completed|failed]) with Lombok annotations. Create DTOs: `CreateTransactionRequest`, `TransactionResponse`. Use BigDecimal for amount and ISO 8601 instant for timestamp. Save under homework-1/src/main/java/com/example/banking/model and dto packages."
- In Copilot Chat:
  "Implement an in-memory repository `TransactionRepository` using a thread-safe map and atomic id generator. Provide methods: save, findById, findAll, findByFilters(accountId, type, fromDate, toDate). Save under repository package."
- Review generated code. If any class is misplaced or package differs, ask:
  "Refactor generated classes to package `com.example.banking` and fix imports."

---

## 3) Validation Rules and Error Handling

- In Copilot Chat:
  "Add validation to `CreateTransactionRequest`: amount > 0, max 2 decimal places; account format ACC-XXXXX (alphanumeric); currency must be supported ISO 4217 (include a whitelist for USD, EUR, GBP, JPY, etc.). Use Jakarta Validation annotations and custom validators where needed."
- In Copilot Chat:
  "Create `GlobalExceptionHandler` with `@ControllerAdvice` to return structured validation errors: { error: 'Validation failed', details: [ { field, message } ] }. Handle 400 and 404 appropriately."
- Review responses format against TASKS.md; if mismatched, prompt:
  "Adjust error response JSON exactly to the required schema in TASKS.md."
- Take screenshot: Copilot prompt and `GlobalExceptionHandler` preview.

---

## 4) Service Layer and Business Logic

- In Copilot Chat:
  "Create `TransactionService` handling: create transaction (apply validation, set id, set timestamp, set status completed), get by id, list with filters, and compute account balance across transactions (deposits add, withdrawals subtract, transfers debit fromAccount and credit toAccount)."
- Ensure currency handling is consistent (no conversion). If Copilot adds conversion, ask to remove it.

---

## 5) REST Controllers

- In Copilot Chat:
  "Create `TransactionController` with endpoints: POST /transactions, GET /transactions, GET /transactions/{id}. Bind query params for filters: accountId, type, from, to (ISO date). Use DTOs for requests/responses. Return 201 on create with Location header."
- In Copilot Chat:
  "Create `AccountController` with GET /accounts/{accountId}/balance that returns current balance as JSON."
- Review method signatures and HTTP status codes (200/201/400/404). If needed:
  "Regenerate controllers to match exact endpoints and status codes from TASKS.md."
- Take screenshot: controller files and endpoints.

---

## 6) Filtering (Task 3)

- In Copilot Chat:
  "Implement filtering in GET /transactions: by accountId, type, from (inclusive), to (inclusive). Combine filters when multiple are provided. Use repository `findByFilters`."
- Verify from/to are applied using transaction timestamp. If off by timezone, ask Copilot to normalize using UTC.

---

## 7) Additional Feature (choose A)

- In Copilot Chat:
  "Add endpoint GET /accounts/{accountId}/summary returning: totalDeposits, totalWithdrawals, transactionCount, mostRecentTransactionDate. Implement in service and controller."
- If Copilot adds extra fields, ask it to conform to spec.

---

## 8) OpenAPI and Sample Requests

- In Copilot Chat:
  "Add Springdoc OpenAPI configuration and ensure Swagger UI is available at /swagger-ui.html. Document models, validation constraints, and response codes with annotations."
- In Copilot Chat:
  "Create demo files under homework-1/demo: run.sh to start the app with Gradle wrapper; sample-requests.http with POST/GET examples for all endpoints; sample-data.json with a few example transactions. Make run.sh executable-friendly for macOS."

---

## 9) Run and Test Locally

- Terminal:
  ```bash
  cd homework-1
  ./gradlew wrapper
  ./gradlew clean build
  ./gradlew bootRun
  ```
- Use VS Code REST Client or curl to test. If errors occur, copy the error into Copilot Chat and ask:
  "Fix the following error in the generated code: <paste error>. Explain the change briefly and provide corrected code only."
- Take screenshot: app starting log and successful sample requests.

---

## 10) Documentation

- In Copilot Chat:
  "Generate README.md for homework-1 with: project overview, endpoints, validation rules, additional feature summary, screenshots placeholders, and how to access Swagger UI. Keep it concise."
- In Copilot Chat:
  "Generate HOWTORUN.md with precise steps for macOS: JDK 21 required, build and run commands, how to execute sample-requests.http, and how to stop the app."
- Ensure screenshots are saved under homework-1/docs/screenshots (ai-prompt-1.png, ai-prompt-2.png, api-running.png). Add any extra as needed.

---

## 11) Code Cleanup and Iteration

- In Copilot Chat:
  "Review the project for consistency: package names, DTO usage, validation messages, and HTTP status codes. Suggest minimal refactors to improve clarity without adding complexity."
- Apply suggestions. Rebuild and rerun quick tests.

---

## 12) Git Workflow and Submission

- Stage and commit iteratively at key milestones (after scaffold, after endpoints, after validation, after tests):
  ```bash
  git add .
  git commit -m "Scaffold Spring Boot Gradle Java 21 project"
  git commit -m "Implement models, repository, service"
  git commit -m "Add controllers, validation, filtering"
  git commit -m "Add summary endpoint, docs, demo files"
  ```
- Final push:
  ```bash
  git push -u origin homework-1-submission
  ```
- Create Pull Request per course README instructions. Attach screenshots.

---

## Copilot Prompt Snippets (Copy/Paste)

1) Scaffold project:
"Create a new Java 21 Spring Boot project inside homework-1 using Gradle (Kotlin DSL). GroupId: com.example, artifactId: banking-api. Add spring-boot-starter-web, validation, lombok, springdoc-openapi-ui. Generate Gradle wrapper and basic Application."

2) Model + DTOs:
"Create Transaction model and CreateTransactionRequest/TransactionResponse DTOs per spec (BigDecimal amount, ISO 8601 timestamp)."

3) Validation + errors:
"Add Jakarta Validation and custom validators; add ControllerAdvice returning the exact validation error schema from TASKS.md."

4) Controllers:
"Implement POST /transactions, GET /transactions, GET /transactions/{id}, GET /accounts/{accountId}/balance with correct status codes and headers."

5) Filters:
"Support accountId, type, from, to filters on GET /transactions (UTC, inclusive)."

6) Summary endpoint:
"Add GET /accounts/{accountId}/summary returning totals and most recent date."

7) Docs + demo:
"Add Swagger via Springdoc and create demo/run.sh and demo/sample-requests.http."

---

## Screenshot Moments

- After project scaffold is generated (Explorer + build files)
- After controllers and validators are created (diff view)
- App running in terminal (bootRun)
- REST requests (HTTP file or curl) showing success and validation errors

---

## Done

Follow the steps sequentially, using only Copilot Chat to generate/modify code and files. Keep prompts short, iterate when output isn’t precise, and capture screenshots along the way.
