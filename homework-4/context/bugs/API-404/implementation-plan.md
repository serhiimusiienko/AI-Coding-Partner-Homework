# Implementation Plan — Bug API-404

> **Prepared by**: Bug Planner  
> **Date**: 9 March 2026  
> **Based on**: `research/verified-research.md` (Quality: GOOD, 81/100)  
> **Target**: Fix `GET /api/users/:id` returning 404 for all valid user IDs

---

## Prerequisites

Run the following once from the project root before applying any changes:

```bash
cd homework-4/demo-bug-fix
npm install
```

Verify the server starts and the bug is reproducible:

```bash
node server.js &
curl -s http://localhost:3000/api/users/123
# Expected (broken):  {"error":"User not found"}
# Confirms bug is present before fix
kill %1
```

---

## Execution Order

1. [Change 1](#change-1--fix-type-mismatch-and-add-input-validation) — Fix type mismatch + add NaN guard in `userController.js`
2. [Verify end-to-end](#verification-plan) — Run the full verification plan

> Only one source file needs to change. No dependency updates, no configuration changes.

---

## Change 1 — Fix type mismatch and add input validation

**File**: `homework-4/demo-bug-fix/src/controllers/userController.js`

**Location**: Lines 18–23 (the `getUserById` function preamble)

**Before** (exact quote from source):

```javascript
async function getUserById(req, res) {
  const userId = req.params.id;

  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
  const user = users.find(u => u.id === userId);
```

**After**:

```javascript
async function getUserById(req, res) {
  const userId = Number(req.params.id);

  if (Number.isNaN(userId)) {
    return res.status(400).json({ error: 'Invalid user ID' });
  }

  const user = users.find(u => u.id === userId);
```

**Rationale**:  
`req.params.id` is always a `string` (e.g. `"123"`). The `users` array stores IDs as `number` (e.g. `123`). The strict equality operator (`===`) does not coerce types, so `"123" === 123` is always `false` — meaning `users.find()` never matches and returns `undefined`, triggering the 404 branch unconditionally.

Converting with `Number()` turns `"123"` → `123` so strict equality works correctly. The `Number.isNaN` guard rejects non-numeric input (e.g. `/api/users/abc`) with a meaningful `400 Bad Request` instead of a misleading `404`, and removes the now-stale BUG comments.

**Test command** (run with server started in background):

```bash
cd homework-4/demo-bug-fix

# Start server
node server.js &
SERVER_PID=$!
sleep 1

# 1. Valid ID — must return user object with 200
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/users/123
# Expected: 200

curl -s http://localhost:3000/api/users/123
# Expected: {"id":123,"name":"Alice Smith","email":"alice@example.com"}

# 2. Another valid ID
curl -s http://localhost:3000/api/users/456
# Expected: {"id":456,"name":"Bob Johnson","email":"bob@example.com"}

# 3. Non-existent numeric ID — still 404 (correct behaviour)
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/users/999
# Expected: 404

# 4. Non-numeric ID — 400 Bad Request (new guard)
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/users/abc
# Expected: 400

# 5. GET /api/users still works
curl -s http://localhost:3000/api/users | head -c 80
# Expected: JSON array starting with [{"id":123,...

kill $SERVER_PID
```

---

## Rollback Plan

If the change causes regressions, restore the original three lines:

```bash
cd homework-4/demo-bug-fix
git diff src/controllers/userController.js   # review what changed
git checkout -- src/controllers/userController.js
```

This reverts `userController.js` to its last committed state. No other files are modified by this plan, so no further rollback is needed.

---

## Verification Plan

End-to-end test confirming the complete fix across all affected scenarios:

```bash
cd homework-4/demo-bug-fix
node server.js &
SERVER_PID=$!
sleep 1

echo "--- 1. Happy path: known IDs ---"
for ID in 123 456 789; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/users/$ID)
  BODY=$(curl -s http://localhost:3000/api/users/$ID)
  echo "GET /api/users/$ID → HTTP $STATUS | $BODY"
done
# All three must return HTTP 200 with the correct user object

echo "--- 2. Unknown numeric ID ---"
curl -s -w "\nHTTP %{http_code}\n" http://localhost:3000/api/users/999
# Must return HTTP 404 {"error":"User not found"}

echo "--- 3. Invalid (non-numeric) ID ---"
curl -s -w "\nHTTP %{http_code}\n" http://localhost:3000/api/users/abc
# Must return HTTP 400 {"error":"Invalid user ID"}

echo "--- 4. List endpoint unaffected ---"
curl -s -w "\nHTTP %{http_code}\n" http://localhost:3000/api/users
# Must return HTTP 200 with full users array (3 objects)

echo "--- 5. Health check unaffected ---"
curl -s -w "\nHTTP %{http_code}\n" http://localhost:3000/health
# Must return HTTP 200 {"status":"ok",...}

kill $SERVER_PID
```

**Pass criteria**:

| Scenario | Expected HTTP | Expected body |
|----------|:---:|---|
| `GET /api/users/123` | 200 | `{"id":123,"name":"Alice Smith","email":"alice@example.com"}` |
| `GET /api/users/456` | 200 | `{"id":456,"name":"Bob Johnson","email":"bob@example.com"}` |
| `GET /api/users/789` | 200 | `{"id":789,"name":"Charlie Brown","email":"charlie@example.com"}` |
| `GET /api/users/999` | 404 | `{"error":"User not found"}` |
| `GET /api/users/abc` | 400 | `{"error":"Invalid user ID"}` |
| `GET /api/users` | 200 | Array with 3 user objects |
| `GET /health` | 200 | `{"status":"ok",...}` |

All 7 scenarios must pass for the fix to be considered complete.

---

## References

| # | File | Role |
|---|------|------|
| 1 | `context/bugs/API-404/bug-context.md` | Original bug report |
| 2 | `context/bugs/API-404/research/verified-research.md` | Verified root-cause analysis |
| 3 | `demo-bug-fix/src/controllers/userController.js` | Only file to modify |
| 4 | `demo-bug-fix/server.js` | Entry point (no changes needed) |
| 5 | `demo-bug-fix/src/routes/users.js` | Router (no changes needed) |
