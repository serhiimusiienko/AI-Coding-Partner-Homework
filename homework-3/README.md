# 📄 Homework 3: Specification-Driven Design

> **Student Name**: Serhii Musiienko  
> **Date Submitted**: 16.02.2026  
> **AI Tools Used**: GitHub Copilot (VS Code)

---

## 📋 Student & Task Summary

This homework delivers a **specification package** for a Virtual Card Management service in a banking application. The package includes a structured specification (`specification.md`), agent guidelines (`agents.md`), and GitHub Copilot rules (`.github/copilot-instructions.md`). **No code was implemented**—only design documents that enable AI-driven development while enforcing FinTech compliance, security, and audit requirements.

---

## 🧠 Rationale

- **Abstraction over specifics**: Tech stack is described abstractly (relational DB, build tool) with reference implementations (H2, Gradle) to keep the spec reusable across environments and vendors.
- **PCI DSS compliance first**: Card data handling (masked PAN, no plain-text storage, token-based references) is a non-negotiable domain constraint reflected in every layer of the spec.
- **Audit-first design**: Every state mutation must generate an immutable audit log entry—this is a FinTech requirement and appears in mid-level objectives, implementation notes, and low-level tasks.
- **Granular, explicit tasks**: Tasks are small (≤15 lines each), constraints are explicit (BigDecimal for money, Bean Validation at API boundary), and requirements are unambiguous so an AI agent can implement without guesswork.
- **Separation of concerns**: `specification.md` drives **what** to build; `agents.md` defines **how** to think (domain rules, patterns); `.github/copilot-instructions.md` is a quick-reference checklist for **runtime AI assistance**.
- **Testing as a first-class citizen**: Testing expectations (unit, integration, fixtures, coverage) appear in every doc to ensure quality gates are built into the workflow from day one.

---

## 🏦 Industry Best Practices

| Practice | Where It Appears |
|----------|------------------|
| **PCI DSS compliance** (mask/tokenize PAN, no plain-text storage) | `specification.md` → Implementation Notes → Security & PCI DSS<br>`agents.md` → Domain Rules (Banking/PCI DSS)<br>`.github/copilot-instructions.md` → Security — Card Data |
| **BigDecimal for money** (never float/double) | `specification.md` → Implementation Notes → Money Handling<br>`agents.md` → Domain Rules → all money fields<br>`.github/copilot-instructions.md` → Money & Numbers<br>`.github/copilot-instructions.md` → Forbidden |
| **Immutable audit trail** (actor, timestamp, old/new values) | `specification.md` → Mid-Level Objectives → Audit & compliance<br>`specification.md` → Task 1 (AuditLogEntry entity)<br>`agents.md` → Domain Rules → audit every state change<br>`.github/copilot-instructions.md` → Audit Logging |
| **Input validation at API boundary** (Bean Validation, reject unexpected fields) | `specification.md` → Implementation Notes → Security & PCI DSS<br>`specification.md` → Task 4 (use @Valid on DTOs)<br>`agents.md` → Code Style → Validation<br>`.github/copilot-instructions.md` → Security — General |
| **Scoped authorization** (user can only access own data, ops role separation) | `specification.md` → Implementation Notes → Security & PCI DSS<br>`specification.md` → Task 4 (cardholder vs ops endpoints)<br>`agents.md` → Security → Authorization<br>`.github/copilot-instructions.md` → Security — General |
| **DTOs instead of entity exposure** (no JPA entities in API responses) | `specification.md` → Task 4 (use Java records)<br>`agents.md` → Code Style → DTOs<br>`.github/copilot-instructions.md` → DTOs<br>`.github/copilot-instructions.md` → Forbidden |
| **Comprehensive testing** (unit + integration, fixtures, 80% coverage) | `specification.md` → Implementation Notes → Testing<br>`specification.md` → Task 6 (unit + integration tests)<br>`agents.md` → Testing (all sections)<br>`.github/copilot-instructions.md` → Testing — Unit/Integration |
| **Centralized exception handling** (consistent error responses, proper HTTP codes) | `specification.md` → Implementation Notes → Error Handling<br>`specification.md` → Task 5 (GlobalExceptionHandler)<br>`agents.md` → Error handling<br>`.github/copilot-instructions.md` → Error Handling |
| **No secrets in code** (env vars, secret management) | `agents.md` → Security → Secrets<br>`.github/copilot-instructions.md` → Security — General<br>`.github/copilot-instructions.md` → Forbidden |
| **SOLID principles** (single responsibility, dependency injection) | `agents.md` → Code Style → SOLID<br>`.github/copilot-instructions.md` → General<br>`.github/copilot-instructions.md` → Patterns to Use |
