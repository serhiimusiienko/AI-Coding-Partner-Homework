#!/usr/bin/env bash
# Coverage gate hook — blocks git push if test coverage is below 80%.
#
# Invoked as a Copilot PreToolUse hook for the Bash tool.
# Copilot writes the tool input as JSON to stdin:
#   {"tool_name": "Bash", "tool_input": {"command": "..."}}

set -euo pipefail

# Read and parse the full hook payload from stdin.
PAYLOAD=$(cat)

# Extract the bash command from tool_input.command.
COMMAND=$(echo "$PAYLOAD" | python3 -c "
import json, sys
d = json.load(sys.stdin)
print(d.get('tool_input', {}).get('command', ''))
")

# Only gate on actual git push invocations.
# Use grep to match "git push" as a command token (at line start or after && ; |),
# not inside a quoted commit message like: git commit -m "...git push..."
if ! echo "$COMMAND" | grep -qE '(^|&&|;|\|)\s*git push($|\s)'; then
  exit 0
fi

# Resolve the homework-6/ directory relative to this script's location.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "[coverage-gate] git push detected — running test coverage check..." >&2

# Run pytest with coverage; capture output for parsing.
COVERAGE_OUTPUT=$(uv run pytest --cov=. --cov-report=term-missing -q 2>&1 || true)

echo "$COVERAGE_OUTPUT" >&2

# Parse the TOTAL line: "TOTAL  <stmts>  <miss>  <cover>%"
COVERAGE_PCT=$(echo "$COVERAGE_OUTPUT" | grep "^TOTAL" | awk '{print $NF}' | tr -d '%')

if [[ -z "$COVERAGE_PCT" ]]; then
  echo "[coverage-gate] ERROR: Could not determine coverage — no TOTAL line found. Blocking push." >&2
  exit 2
fi

if (( COVERAGE_PCT < 80 )); then
  echo "[coverage-gate] BLOCKED: Coverage is ${COVERAGE_PCT}% — minimum required is 80%. Fix tests before pushing." >&2
  exit 2
fi

echo "[coverage-gate] Coverage is ${COVERAGE_PCT}% — OK. Allowing push." >&2
exit 0
