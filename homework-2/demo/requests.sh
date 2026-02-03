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

# Helper: run curl, print HTTP status to stderr and pretty body to stdout;
# useful for capturing the response body in a variable while still showing
# the HTTP status to the user.
request_capture() {
  local tmp curl_exit http_code
  tmp=$(mktemp)
  curl_exit=0
  http_code=$(curl -sS -w "%{http_code}" -o "$tmp" "$@") || curl_exit=$?
  if [ "$curl_exit" -ne 0 ]; then
    echo "curl failed with exit code $curl_exit" >&2
  fi
  # Print HTTP status to stderr so stdout can be captured as the response body
  echo "HTTP $http_code" >&2
  # Pretty-print JSON (or raw) to stdout for capture
  maybe_jq < "$tmp" || true
  rm -f "$tmp"
}

# Helper: run curl, print HTTP status and body (pretty JSON when applicable)
request() {
  local tmp curl_exit http_code
  tmp=$(mktemp)
  curl_exit=0
  http_code=$(curl -sS -w "%{http_code}" -o "$tmp" "$@") || curl_exit=$?
  if [ "$curl_exit" -ne 0 ]; then
    echo "curl failed with exit code $curl_exit" >&2
  fi
  echo "HTTP $http_code"
  maybe_jq < "$tmp" || true
  rm -f "$tmp"
}

echo "# Create a ticket"
# Capture created ticket JSON to extract the ID for later requests
create_response=$(request_capture -X POST "$BASE_URL/tickets?autoClassify=false" \
  -H 'Content-Type: application/json' \
  -d '{"customer_id":"CUST-1","customer_email":"user@example.com","customer_name":"User","subject":"Login issue","description":"Cannot access account"}' || true)

echo "# List tickets"
request "$BASE_URL/tickets" || true

echo "# Import tickets (JSON example)"
if [[ -f "src/test/resources/fixtures/sample_tickets.json" ]]; then
  request -X POST "$BASE_URL/tickets/import" \
    -F "file=@src/test/resources/fixtures/sample_tickets.json" -F "format=json" || true
else
  echo "(src/test/resources/fixtures/sample_tickets.json not found; skipping JSON import example)"
fi

echo "# Import tickets (CSV example)"
if [[ -f "src/test/resources/fixtures/sample_tickets.csv" ]]; then
  request -X POST "$BASE_URL/tickets/import" \
    -F "file=@src/test/resources/fixtures/sample_tickets.csv" -F "format=csv" || true
else
  echo "(src/test/resources/fixtures/sample_tickets.csv not found; skipping CSV import example)"
fi

echo "# Import tickets (XML example)"
if [[ -f "src/test/resources/fixtures/sample_tickets.xml" ]]; then
  request -X POST "$BASE_URL/tickets/import" \
    -F "file=@src/test/resources/fixtures/sample_tickets.xml" -F "format=xml" || true
else
  echo "(src/test/resources/fixtures/sample_tickets.xml not found; skipping XML import example)"
fi

# Extract `id` from JSON if present
ID=""
if command -v jq >/dev/null 2>&1; then
  ID=$(printf '%s' "$create_response" | jq -r '.id // empty') || ID=""
fi
if [ -n "$ID" ]; then
  echo "Created ticket id: $ID"
else
  echo "No ticket id returned; subsequent ID-based requests will be skipped" >&2
fi

if [ -n "$ID" ]; then
  echo "# Get ticket by ID"
  request "$BASE_URL/tickets/$ID" || true

  echo "# Update ticket"
  request -X PUT "$BASE_URL/tickets/$ID" \
    -H 'Content-Type: application/json' -d '{"subject":"Updated subject"}' || true

  echo "# Auto-classify"
  request -X POST "$BASE_URL/tickets/$ID/auto-classify" || true

  echo "# Delete ticket"
  request -X DELETE "$BASE_URL/tickets/$ID" || true
else
  echo "Skipping GET/UPDATE/AUTO-CLASSIFY/DELETE steps because no ID was obtained from create." >&2
fi
