````markdown
# API Reference

Base URL: `http://localhost:8080`  
Content-Type: `application/json` (snake_case)

## Endpoints
- `POST /tickets?autoClassify={true|false}` -> `201 Ticket`
- `POST /tickets/import` (multipart `file`, optional `format=csv|json|xml`) -> `200 ImportSummary`
- `GET /tickets` (optional `category|priority|status|tag|from|to`) -> `200 Ticket[]`
- `GET /tickets/{id}` -> `200 Ticket` / `404`
- `PUT /tickets/{id}` (partial update) -> `200 Ticket` / `404`
- `DELETE /tickets/{id}` -> `204` / `404`
- `POST /tickets/{id}/auto-classify` -> `200 ClassificationResult` / `404`

## Create ticket
```bash
curl -s -X POST 'http://localhost:8080/tickets?autoClassify=true' \
  -H 'Content-Type: application/json' \
  -d '{"customer_id":"C1","customer_email":"u@example.com","customer_name":"User","subject":"Production down","description":"App crash in prod"}'
```

## Import (format inferred by filename)
```bash
curl -s -X POST 'http://localhost:8080/tickets/import' \
  -F 'file=@src/test/resources/fixtures/sample_tickets.csv'
```

## Response shapes (abridged)
- `Ticket`: has `id`, `created_at`, `updated_at`, `status` (default `NEW`), optional `tags`, `metadata`, `classification_confidence`
- `ImportSummary`: `total_records`, `successful`, `failed`, `failures[] { index, error }`
- `ClassificationResult`: `category`, `priority`, `confidence`, `reasoning`, `keywords[]`

## Errors
- `400` invalid payload / malformed import / unknown format (may be empty body)
- `404` unknown ticket id

````
