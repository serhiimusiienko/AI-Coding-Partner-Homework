# HOWTORUN — Homework 4

## Prerequisites
- Node.js (v18+ recommended)
- VS Code with Copilot Agent mode

## Setup
1. Clone the repo and open in VS Code.
2. Install dependencies:
   ```bash
   cd homework-4/demo-bug-fix
   npm install
   ```
3. Set up agents:
   - Copy `.agent.md` files from `agents/` to `.github/agents/`
4. Set up skills:
   - Copy skill directories from `skills/` to `.github/skills/`

## Run the Pipeline
1. Run each agent in sequence:
   - Bug Research Verifier → Bug Implementer → Security Verifier → Unit Test Generator
2. Reference screenshots in `docs/screenshots/` for each step.

## Run the Demo App
1. Start the server:
   ```bash
   cd homework-4/demo-bug-fix
   node server.js
   ```
2. Test endpoints:
   ```bash
   curl http://localhost:3000/api/users/123
   ```

## Run the Tests
1. Run unit tests:
   ```bash
   npm test
   ```

## Verify Fix
- Check agent outputs in `context/bugs/API-404/`
- Review screenshots in `docs/screenshots/`
