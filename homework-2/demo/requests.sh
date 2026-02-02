#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}

# Helper: pretty-print JSON if response looks like JSON, otherwise print as-is
maybe_jq() {
  local body first
  body=$(cat)
  first=$(printf '%s' "$body" | sed -n '1s/^[[:space:]]*\(.*\)$/\1/p' | cut -c1)
  if [[ "$first" == "{" || "$first" == "[" ]]; then
    printf '%s\n' "$body" | jq '.'
  else
    printf '%s\n' "$body"
  fi
}

echo "# Create a ticket"
curl -sS -X POST "$BASE_URL/tickets?autoClassify=false" \
  -H 'Content-Type: application/json' \
  -d '{"customer_id":"CUST-1","customer_email":"user@example.com","customer_name":"User","subject":"Login issue","description":"Cannot access account"}' \
  | maybe_jq || true

echo "# List tickets"
curl -sS "$BASE_URL/tickets" | maybe_jq || true

echo "# Import tickets (CSV/JSON/XML) - placeholder; adjust with -F file=@path"
curl -sS -X POST "$BASE_URL/tickets/import" | maybe_jq || true

echo "# Get ticket by ID (placeholder ID)"
curl -sS "$BASE_URL/tickets/00000000-0000-0000-0000-000000000000" | maybe_jq || true

echo "# Update ticket (placeholder ID)"
curl -sS -X PUT "$BASE_URL/tickets/00000000-0000-0000-0000-000000000000" \
  -H 'Content-Type: application/json' -d '{"subject":"Updated subject"}' \
  | maybe_jq || true

echo "# Delete ticket (placeholder ID)"
curl -sS -X DELETE "$BASE_URL/tickets/00000000-0000-0000-0000-000000000000" | maybe_jq || true

echo "# Auto-classify (placeholder ID)"
curl -sS -X POST "$BASE_URL/tickets/00000000-0000-0000-0000-000000000000/auto-classify" | maybe_jq || true
