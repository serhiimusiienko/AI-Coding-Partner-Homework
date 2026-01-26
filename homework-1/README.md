# 🏦 Homework 1: Banking Transactions API (Java 21 + Spring Boot)

> **Student Name**: [Your Name]
> **Date Submitted**: [Date]
> **AI Tools Used**: GitHub Copilot

---

## 📋 Overview

A minimal REST API for banking transactions implemented with Java 21, Gradle, and Spring Boot. Uses in-memory storage and includes validation, filtering, and an account summary endpoint.

## ✅ Endpoints

- POST `/transactions` — Create transaction
- GET `/transactions` — List transactions (filters: `accountId`, `type`, `from`, `to` — ISO 8601)
- GET `/transactions/{id}` — Get transaction by ID
- GET `/accounts/{accountId}/balance` — Get account balance
- GET `/accounts/{accountId}/summary` — Summary (deposits, withdrawals, count, most recent date)

## 🔎 Validation

- `amount` > 0 and max 2 decimal places
- `fromAccount` and `toAccount` match `ACC-XXXXX` (alphanumeric)
- `currency` in allowed set (USD, EUR, GBP, JPY, CHF, CAD, AUD)
- Structured error response:
	```json
	{ "error": "Validation failed", "details": [{"field": "...", "message": "..."}] }
	```

## 🧮 Balance Rules

- `deposit`: credit `toAccount`
- `withdrawal`: debit `fromAccount`
- `transfer`: debit `fromAccount`, credit `toAccount`

## 🧪 Try It

- Start the app: `./gradlew bootRun` (port `3000`)
- Use the demo HTTP file: `demo/sample-requests.http`
- Swagger UI: `/swagger-ui/index.html`

## 📂 Structure

- `src/main/java/com/example/banking/` — app code
- `web/` — controllers and exception handler
- `service/` — business logic
- `repository/` — in-memory storage
- `dto/` — request/response models
- `validation/` — custom validators

## 🛠 Tech

- Java 21, Gradle, Spring Boot (web, validation), Springdoc OpenAPI

<div align="center">

*Project completed with AI assistance.*

</div>
