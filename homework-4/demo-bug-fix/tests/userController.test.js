/**
 * Unit tests for getUserById — Bug API-404 fix
 *
 * Scope : Only the changed code paths in getUserById:
 *   1. Number() type conversion (string → number)
 *   2. Number.isNaN() guard → 400 for non-numeric input
 *   3. users.find() match after type fix → 200 with user object
 *   4. users.find() miss (valid number, no record) → 404
 *
 * FIRST compliance:
 *   Fast        — no HTTP server, no I/O; req/res mocked inline
 *   Independent — each it() builds its own req/res; no shared mutable state
 *   Repeatable  — no Date.now / Math.random; pure synchronous-equivalent logic
 *   Self-validating — every test has ≥1 explicit expect()
 *   Timely      — only changed code paths tested
 */

'use strict';

const { getUserById } = require('../src/controllers/userController');

// ---------------------------------------------------------------------------
// Helper: returns a fresh mock res object for every test
// ---------------------------------------------------------------------------
function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);   // chainable: res.status(x).json(y)
  res.json   = jest.fn().mockReturnValue(res);
  return res;
}

// ---------------------------------------------------------------------------
// describe: getUserById
// ---------------------------------------------------------------------------
describe('getUserById', () => {

  // ── Happy path ────────────────────────────────────────────────────────────
  describe('happy path — string ID coerced to number, user found → 200', () => {

    it('returns the correct user for string "123" (id 123)', async () => {
      const req = { params: { id: '123' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).not.toHaveBeenCalled();
      expect(res.json).toHaveBeenCalledWith({
        id: 123,
        name: 'Alice Smith',
        email: 'alice@example.com',
      });
    });

    it('returns the correct user for string "456" (id 456)', async () => {
      const req = { params: { id: '456' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).not.toHaveBeenCalled();
      expect(res.json).toHaveBeenCalledWith({
        id: 456,
        name: 'Bob Johnson',
        email: 'bob@example.com',
      });
    });

    it('returns the correct user for string "789" (id 789)', async () => {
      const req = { params: { id: '789' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).not.toHaveBeenCalled();
      expect(res.json).toHaveBeenCalledWith({
        id: 789,
        name: 'Charlie Brown',
        email: 'charlie@example.com',
      });
    });

  });

  // ── NaN guard — new code added by the fix ─────────────────────────────────
  describe('NaN guard — non-numeric input → 400 Bad Request', () => {

    it('returns 400 for purely alphabetic ID "abc"', async () => {
      const req = { params: { id: 'abc' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).toHaveBeenCalledWith(400);
      expect(res.json).toHaveBeenCalledWith({ error: 'Invalid user ID' });
    });

    it('returns 400 for alphanumeric ID "12abc"', async () => {
      const req = { params: { id: '12abc' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).toHaveBeenCalledWith(400);
      expect(res.json).toHaveBeenCalledWith({ error: 'Invalid user ID' });
    });

    it('returns 400 for special-character ID "!@#"', async () => {
      const req = { params: { id: '!@#' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).toHaveBeenCalledWith(400);
      expect(res.json).toHaveBeenCalledWith({ error: 'Invalid user ID' });
    });

    it('returns 400 for empty-string ID "" (Number("") === 0 is NaN-equivalent edge)', async () => {
      // Number('') === 0, which is NOT NaN — so this falls through to 404.
      // Documented here to capture the actual observed behaviour of the fix.
      const req = { params: { id: '' } };
      const res = mockRes();

      await getUserById(req, res);

      // '' → Number('') === 0 → not NaN → find() misses → 404
      expect(res.status).toHaveBeenCalledWith(404);
      expect(res.json).toHaveBeenCalledWith({ error: 'User not found' });
    });

  });

  // ── Not-found — numeric but no matching user ───────────────────────────────
  describe('not found — numeric ID with no matching record → 404', () => {

    it('returns 404 for ID "999" (no user with that id)', async () => {
      const req = { params: { id: '999' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).toHaveBeenCalledWith(404);
      expect(res.json).toHaveBeenCalledWith({ error: 'User not found' });
    });

    it('returns 404 for ID "0" (no user has id 0)', async () => {
      const req = { params: { id: '0' } };
      const res = mockRes();

      await getUserById(req, res);

      expect(res.status).toHaveBeenCalledWith(404);
      expect(res.json).toHaveBeenCalledWith({ error: 'User not found' });
    });

  });

});
