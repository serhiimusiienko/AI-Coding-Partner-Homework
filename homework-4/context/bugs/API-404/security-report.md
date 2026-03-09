# Security Report — Bug API-404

> **Reviewed by**: Security Verifier Agent
> **Date**: 10 March 2026
> **Input**: `context/bugs/API-404/fix-summary.md`
> **Changed file**: `demo-bug-fix/src/controllers/userController.js`

---

## Executive Summary

```
Total findings :  7
  CRITICAL     :  0
  HIGH         :  0
  MEDIUM       :  2
  LOW          :  3
  INFO         :  2
Overall risk   :  MEDIUM
```

---

## Findings

| # | Severity | Category | File:Line | Description |
|---|----------|----------|-----------|-------------|
| 1 | MEDIUM | Info Disclosure / Missing Auth | `src/controllers/userController.js:39` | `getAllUsers` returns every user's PII (id, name, email) to any unauthenticated caller |
| 2 | MEDIUM | Missing Auth / Info Disclosure | `src/routes/users.js:11,14` | Both `/api/users` and `/api/users/:id` endpoints have no authentication or authorization |
| 3 | LOW | Missing Validation | `src/controllers/userController.js:20` | `Number()` converts an empty string `""` to `0`; while Express won't route `/api/users/` to this handler, adjacent edge cases (e.g. very large numbers `1e308`) are silently accepted |
| 4 | LOW | Info Disclosure | `server.js:1-32` | No `helmet` middleware — Express adds `X-Powered-By: Express` response header, disclosing the framework to attackers |
| 5 | LOW | Info Disclosure | `server.js:1-32` | No global error handler — Express's default handler can send stack traces to the client in development mode or when unhandled exceptions occur |
| 6 | INFO | Missing Validation | `src/routes/users.js:14` | No rate limiting on any endpoint; the user-by-ID route is trivially brute-forceable to enumerate all valid IDs |
| 7 | INFO | Info Disclosure | `server.js:19-21` | `GET /health` leaks internal messaging and confirms the server is live; no auth or CIDR restriction |

---

### Finding 1 — MEDIUM: Unauthenticated PII exposure in `getAllUsers`

**Code snippet** (`src/controllers/userController.js:37-39`):

```javascript
async function getAllUsers(req, res) {
  res.json(users);
}
```

**Remediation**: Add an authentication middleware (JWT, API key, or session check) before `getAllUsers`. If public listing is required, strip sensitive fields (e.g. email) from the response, or paginate and require an authenticated token.

---

### Finding 2 — MEDIUM: No authentication on any user endpoint

**Code snippet** (`src/routes/users.js:11,14`):

```javascript
router.get('/api/users', userController.getAllUsers);
router.get('/api/users/:id', userController.getUserById);
```

**Remediation**: Apply an `authenticate` middleware before these routes:

```javascript
router.get('/api/users',     authenticate, userController.getAllUsers);
router.get('/api/users/:id', authenticate, userController.getUserById);
```

---

### Finding 3 — LOW: Edge-case inputs silently accepted after `Number()` conversion

**Code snippet** (`src/controllers/userController.js:20-23`):

```javascript
const userId = Number(req.params.id);

if (Number.isNaN(userId)) {
  return res.status(400).json({ error: 'Invalid user ID' });
}
```

`Number("1e308")` → `Infinity` (not NaN, passes the guard).
`Number("0")` → `0` (valid, just won't match any user — returns 404, not harmful).

**Remediation**: Use `Number.isInteger()` + a positive-integer check instead of `Number()` + `isNaN`:

```javascript
const userId = parseInt(req.params.id, 10);
if (!Number.isInteger(userId) || userId <= 0) {
  return res.status(400).json({ error: 'Invalid user ID' });
}
```

---

### Finding 4 — LOW: Missing `helmet` — `X-Powered-By: Express` header leaked

**Code snippet** (`server.js:6,9,13`):

```javascript
const express = require('express');
const app = express();
app.use(express.json());
// no helmet
```

**Remediation**: Install and apply `helmet`:

```bash
npm install helmet
```

```javascript
const helmet = require('helmet');
app.use(helmet());   // disables X-Powered-By, sets secure headers
```

---

### Finding 5 — LOW: No global error handler — potential stack-trace leakage

**Code snippet** (`server.js:17-21` — absence of error middleware):

```javascript
app.use(userRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Demo API is running' });
});
// No app.use((err, req, res, next) => { ... }) present
```

**Remediation**: Add a final error-handling middleware before `app.listen`:

```javascript
app.use((err, req, res, next) => {
  console.error(err.stack);                  // log internally
  res.status(500).json({ error: 'Internal server error' });
});
```

---

### Finding 6 — INFO: No rate limiting

**Code snippet** (`src/routes/users.js:14`):

```javascript
router.get('/api/users/:id', userController.getUserById);
```

**Remediation**: Apply `express-rate-limit`:

```javascript
const rateLimit = require('express-rate-limit');
const limiter = rateLimit({ windowMs: 15 * 60 * 1000, max: 100 });
router.get('/api/users/:id', limiter, userController.getUserById);
```

---

### Finding 7 — INFO: Unauthenticated health-check endpoint

**Code snippet** (`server.js:19-21`):

```javascript
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Demo API is running' });
});
```

**Remediation**: For production, restrict to internal network or add a token check. At minimum suppress the `X-Powered-By` header (covered by Finding 4 / `helmet`).

---

## Positive Observations

- **`Number.isNaN()` used correctly** — the safer static method is used instead of the global `isNaN()`, which coerces arguments and can produce false negatives.
- **Generic error messages** — `"User not found"` and `"Invalid user ID"` do not expose internal paths, array contents, or stack traces.
- **Proper HTTP status codes** — `400` for bad input, `404` for missing resource, `200` for success; semantically correct.
- **`res.json()` used exclusively** — sets `Content-Type: application/json` automatically; no raw HTML output, eliminating XSS vectors in this context.
- **No hardcoded secrets** — no API keys, passwords, or tokens appear anywhere in the codebase.
- **No injection surface** — data is served from an in-memory array; there are no SQL/NoSQL queries, shell commands, or template engines that could be injected into.
- **`express.json()` body parser scoped to app** — prevents raw-body DoS on JSON parsing with Express's default 100 kb limit.
- **Input validated before use** — `Number.isNaN` guard added by the fix prevents the controller logic from running on clearly invalid input.

---

## Recommendations

Prioritized most-critical first:

1. **[MEDIUM]** Add authentication middleware to all `/api/users*` routes before going to production.
2. **[MEDIUM]** Audit what fields are returned by `getAllUsers`; strip or omit PII (email) if unauthenticated access is required.
3. **[LOW]** Replace `Number()` + `isNaN` guard with `parseInt(..., 10)` + positive-integer validation to eliminate `Infinity` and zero-ID edge cases.
4. **[LOW]** Install `helmet` (`npm install helmet` + `app.use(helmet())`) to set secure HTTP headers and suppress `X-Powered-By`.
5. **[LOW]** Add a global Express error handler to prevent accidental stack-trace leakage.
6. **[INFO]** Apply `express-rate-limit` to the user-lookup endpoint to prevent ID enumeration.
7. **[INFO]** Restrict or remove the `/health` endpoint in production, or move it behind a CIDR allowlist.

---

## Scan Scope

- `homework-4/demo-bug-fix/src/controllers/userController.js` (directly modified by the fix)
- `homework-4/demo-bug-fix/src/routes/users.js` (references changed controller)
- `homework-4/demo-bug-fix/server.js` (entry point; configures middleware and mounts routes)
- `homework-4/demo-bug-fix/package.json` (dependencies and scripts)
