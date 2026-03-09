# Fix Summary — Bug API-404

> **Implemented by**: Bug Implementer Agent
> **Date**: 9 March 2026
> **Plan**: `context/bugs/API-404/implementation-plan.md`

---

## Changes Made

| # | File | Location | Before | After | Test Result |
|---|------|----------|--------|-------|-------------|
| 1 | `demo-bug-fix/src/controllers/userController.js` | Lines 18–25 | See below | See below | ✅ Pass |

### Change 1 — Full before/after

**Before:**

```javascript
async function getUserById(req, res) {
  const userId = req.params.id;

  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
  const user = users.find(u => u.id === userId);
```

**After:**

```javascript
async function getUserById(req, res) {
  const userId = Number(req.params.id);

  if (Number.isNaN(userId)) {
    return res.status(400).json({ error: 'Invalid user ID' });
  }

  const user = users.find(u => u.id === userId);
```

---

## Overall Status

```
Status: ALL CHANGES APPLIED SUCCESSFULLY
Total changes: 1 / 1
```

---

## Test Results

```
Tests run   : 7
Passed      : 7
Failed      : 0
Last command : curl series against http://localhost:3000
Last output  : all green
```

### Scenario detail

| # | Scenario | Expected HTTP | Actual HTTP | Body |
|---|----------|:---:|:---:|---|
| 1 | `GET /api/users/123` | 200 | ✅ 200 | `{"id":123,"name":"Alice Smith","email":"alice@example.com"}` |
| 2 | `GET /api/users/456` | 200 | ✅ 200 | `{"id":456,"name":"Bob Johnson","email":"bob@example.com"}` |
| 3 | `GET /api/users/789` | 200 | ✅ 200 | `{"id":789,"name":"Charlie Brown","email":"charlie@example.com"}` |
| 4 | `GET /api/users/999` | 404 | ✅ 404 | `{"error":"User not found"}` |
| 5 | `GET /api/users/abc` | 400 | ✅ 400 | `{"error":"Invalid user ID"}` |
| 6 | `GET /api/users` | 200 | ✅ 200 | Array of 3 user objects |
| 7 | `GET /health` | 200 | ✅ 200 | `{"status":"ok","message":"Demo API is running"}` |

---

## Manual Verification Steps

```bash
# Navigate to the app directory
cd homework-4/demo-bug-fix

# Install dependencies (first time only)
npm install

# Start the server
node server.js

# In a separate terminal — primary fix (was 404, now 200)
curl -i http://localhost:3000/api/users/123
# Expected: HTTP/1.1 200 OK
# Body: {"id":123,"name":"Alice Smith","email":"alice@example.com"}

# Test all valid IDs
curl -s http://localhost:3000/api/users/456
# Expected: {"id":456,"name":"Bob Johnson","email":"bob@example.com"}

curl -s http://localhost:3000/api/users/789
# Expected: {"id":789,"name":"Charlie Brown","email":"charlie@example.com"}

# Unknown numeric ID still returns 404 (correct)
curl -i http://localhost:3000/api/users/999
# Expected: HTTP/1.1 404 Not Found

# Non-numeric ID returns 400 (new NaN guard)
curl -i http://localhost:3000/api/users/abc
# Expected: HTTP/1.1 400 Bad Request
# Body: {"error":"Invalid user ID"}

# Unaffected endpoints still work
curl -s http://localhost:3000/api/users   # 200 — array of 3 users
curl -s http://localhost:3000/health      # 200 — {"status":"ok",...}
```

---

## References

- `homework-4/demo-bug-fix/src/controllers/userController.js` — only file modified
- `homework-4/context/bugs/API-404/implementation-plan.md` — plan followed
- `homework-4/context/bugs/API-404/research/verified-research.md` — verified research basis
- `homework-4/context/bugs/API-404/bug-context.md` — original bug report
