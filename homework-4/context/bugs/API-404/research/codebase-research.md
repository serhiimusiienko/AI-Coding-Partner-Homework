# Codebase Research — Bug API-404

> **GET /api/users/:id returns 404 for valid user IDs**

## Bug Summary

| Field | Value |
|-------|-------|
| **ID** | API-404 |
| **Title** | GET /api/users/:id returns 404 for every valid user ID |
| **Priority** | High |
| **Symptom** | `{"error":"User not found"}` with HTTP 404 for all individual user lookups |
| **Scope** | 100 % of users calling the single-user endpoint |

---

## Codebase Analysis

### 1. `demo-bug-fix/server.js` (entry point)

| Line(s) | Detail |
|----------|--------|
| 7 | Imports routes: `const userRoutes = require('./src/routes/users');` |
| 16 | Mounts routes: `app.use(userRoutes);` |

The server delegates all `/api/users*` traffic to the router exported by `src/routes/users.js`. No path prefix is added at mount time.

```js
// server.js:7
const userRoutes = require('./src/routes/users');

// server.js:16
app.use(userRoutes);
```

### 2. `demo-bug-fix/src/routes/users.js` (router)

| Line(s) | Detail |
|----------|--------|
| 6–8 | Creates Express router and requires `userController` |
| 11 | `router.get('/api/users', userController.getAllUsers);` |
| 14 | `router.get('/api/users/:id', userController.getUserById);` |

Both routes are correctly defined. The `:id` parameter is captured as `req.params.id`.

```js
// src/routes/users.js:11
router.get('/api/users', userController.getAllUsers);

// src/routes/users.js:14
router.get('/api/users/:id', userController.getUserById);
```

### 3. `demo-bug-fix/src/controllers/userController.js` (controller — **bug location**)

| Line(s) | Detail |
|----------|--------|
| 7–11 | In-memory `users` array with **numeric** `id` values (`123`, `456`, `789`) |
| 20 | `const userId = req.params.id;` — **returns a string** (e.g. `"123"`) |
| 24 | `const user = users.find(u => u.id === userId);` — **strict equality** compares `number === string` → always `false` |
| 26–28 | Because `user` is always `undefined`, the 404 branch is always taken |
| 35–37 | `getAllUsers` simply returns the full array — works correctly |

```js
// src/controllers/userController.js:7-11
const users = [
  { id: 123, name: 'Alice Smith', email: 'alice@example.com' },
  { id: 456, name: 'Bob Johnson', email: 'bob@example.com' },
  { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
];
```

```js
// src/controllers/userController.js:20-28
async function getUserById(req, res) {
  const userId = req.params.id;

  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
  const user = users.find(u => u.id === userId);

  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  res.json(user);
}
```

### 4. `demo-bug-fix/package.json` (dependencies)

| Field | Value |
|-------|-------|
| `express` | `^4.18.2` |
| `nodemon` | `^3.0.1` (dev) |
| `start` script | `node server.js` |

No test framework is configured yet.

---

## Root Cause Analysis

**File**: `demo-bug-fix/src/controllers/userController.js`  
**Line**: 24

```js
const user = users.find(u => u.id === userId);
```

Express route parameters (`req.params.id`) are **always strings**. The `users` array stores IDs as **numbers**. The strict-equality operator (`===`) does not perform type coercion, so `"123" === 123` evaluates to `false`. Consequently `users.find()` never matches any entry and always returns `undefined`, triggering the 404 response on line 26.

---

## Impact Analysis

| Area | Impact |
|------|--------|
| `GET /api/users/:id` | **Broken** — returns 404 for every valid ID |
| `GET /api/users` | Unaffected — returns the full array without ID lookup |
| Downstream consumers | Any client relying on single-user retrieval is blocked |
| Data integrity | None — the data store is not mutated |

Only the `getUserById` function is affected. The fix is isolated to one line (or two, if we also add input validation).

---

## Proposed Fix

Convert `req.params.id` to a number before comparison.

### Before (`userController.js:20-24`)

```js
async function getUserById(req, res) {
  const userId = req.params.id;

  const user = users.find(u => u.id === userId);
```

### After

```js
async function getUserById(req, res) {
  const userId = Number(req.params.id);

  if (Number.isNaN(userId)) {
    return res.status(400).json({ error: 'Invalid user ID' });
  }

  const user = users.find(u => u.id === userId);
```

**Why `Number()` + `isNaN` guard**: Using `Number()` converts `"123"` → `123` so strict equality works. The `isNaN` guard rejects non-numeric input (e.g. `/api/users/abc`) with a 400 instead of letting it fall through to a misleading 404.

---

## References

| # | File | Lines | Purpose |
|---|------|-------|---------|
| 1 | `demo-bug-fix/server.js` | 1–32 | Entry point, route mounting |
| 2 | `demo-bug-fix/src/routes/users.js` | 1–17 | Route definitions |
| 3 | `demo-bug-fix/src/controllers/userController.js` | 1–46 | Controller with bug at line 24 |
| 4 | `demo-bug-fix/package.json` | 1–20 | Dependencies & scripts |
| 5 | `demo-bug-fix/bugs/API-404/bug-context.md` | 1–57 | Original bug report |
