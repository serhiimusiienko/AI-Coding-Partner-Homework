---
name: unit-tests-FIRST
description: >
  Enforces the FIRST principles (Fast, Independent, Repeatable,
  Self-validating, Timely) when generating or reviewing unit tests.
  Use when creating unit tests for new or changed code in a Node.js project.
---

# Unit Tests — FIRST Principles

Apply **every** principle below when generating or reviewing unit tests.

---

## F — Fast

Tests execute in milliseconds with no I/O waits.

- [ ] No real HTTP calls, DB queries, or file-system writes
- [ ] External dependencies replaced with mocks / stubs / spies
- [ ] No `setTimeout`, `sleep`, or arbitrary delays
- [ ] Full suite finishes in under a few seconds

**Anti-pattern**

```js
// ✗ Hits a real database — slow and fragile
it('creates user', async () => {
  const user = await db.query('INSERT INTO users …');
  expect(user.id).toBeDefined();
});
```

**Good pattern**

```js
// ✓ In-memory mock — fast, no I/O
it('creates user', async () => {
  db.query = jest.fn().mockResolvedValue({ id: 1, name: 'Ada' });
  const user = await createUser('Ada');
  expect(user.id).toBe(1);
});
```

---

## I — Independent

Each test is self-contained; order of execution doesn't matter.

- [ ] No shared mutable state between tests
- [ ] Each test sets up and tears down its own data
- [ ] Removing or reordering any test breaks nothing
- [ ] No test reads a value written by another test

**Anti-pattern**

```js
// ✗ Tests share `user` — second test depends on the first
let user;
it('creates user', () => { user = createUser('Ada'); });
it('gets user name', () => { expect(user.name).toBe('Ada'); });
```

**Good pattern**

```js
// ✓ Each test creates its own data
it('creates user', () => {
  const user = createUser('Ada');
  expect(user.id).toBeDefined();
});

it('gets user name', () => {
  const user = createUser('Ada');
  expect(user.name).toBe('Ada');
});
```

---

## R — Repeatable

Same result on any machine, any time, any run count.

- [ ] No reliance on current date/time — inject or mock clocks
- [ ] No random values unless seeded deterministically
- [ ] No dependency on environment variables or host-specific paths
- [ ] Works identically in CI and locally

**Anti-pattern**

```js
// ✗ Fails after midnight or in different timezones
it('checks expiry', () => {
  const token = createToken();
  expect(token.expiresAt.getDate()).toBe(new Date().getDate());
});
```

**Good pattern**

```js
// ✓ Frozen clock — always deterministic
it('checks expiry', () => {
  jest.useFakeTimers({ now: new Date('2026-01-15T00:00:00Z') });
  const token = createToken();
  expect(token.expiresAt).toEqual(new Date('2026-01-16T00:00:00Z'));
  jest.useRealTimers();
});
```

---

## S — Self-Validating

Pass or fail is determined automatically — no manual inspection needed.

- [ ] Every test has at least one explicit assertion
- [ ] Assertion messages describe intent (e.g., `"should return 404 for missing user"`)
- [ ] No `console.log` used as the only verification
- [ ] Edge cases assert specific errors, not just "no crash"

**Anti-pattern**

```js
// ✗ No assertion — always passes, proves nothing
it('fetches users', async () => {
  const res = await getUsers();
  console.log(res);
});
```

**Good pattern**

```js
// ✓ Clear assertion with meaningful expectation
it('returns empty array when no users exist', async () => {
  db.query = jest.fn().mockResolvedValue([]);
  const users = await getUsers();
  expect(users).toEqual([]);
});
```

---

## T — Timely

Tests are written alongside the code change, not as an afterthought.

- [ ] Every new / changed function has a corresponding test
- [ ] Tests cover the happy path **and** at least one error path
- [ ] Tests are committed in the same PR as the production code
- [ ] Coverage is checked: new lines should not decrease overall %

**Anti-pattern**

Shipping `fix-summary.md` with three file changes and zero tests.

**Good pattern**

For each entry in `fix-summary.md`, generate tests that exercise:
1. The corrected behavior (happy path)
2. The original failing behavior (regression guard)
3. Boundary / edge inputs relevant to the fix

---

## Quick Reference

| Principle | One-liner |
|-----------|-----------|
| **F**ast | Milliseconds, no I/O, mock everything external |
| **I**ndependent | No shared state, any order, any subset |
| **R**epeatable | Same result everywhere, deterministic inputs |
| **S**elf-validating | Explicit assertions, no manual checks |
| **T**imely | Tests ship with the code they cover |
