# API Reference

Base URL: `http://localhost:8080`

## Tickets
- Create: `POST /tickets?autoClassify=false`
- List: `GET /tickets?category=TECHNICAL_ISSUE&priority=HIGH`
- Get: `GET /tickets/{id}`
- Update: `PUT /tickets/{id}`
- Delete: `DELETE /tickets/{id}`
- Auto-classify: `POST /tickets/{id}/auto-classify`

### Data Model (JSON snake_case)
Example:
```json
{
  "customer_id":"CUST-1",
  "customer_email":"user@example.com",
  "customer_name":"User",
  "subject":"Login issue",
  "description":"Cannot access account",
  "category":"OTHER",
  "priority":"MEDIUM",
  "status":"NEW",
  "tags":["auth"],
  "metadata": {"source":"web_form","browser":"Chrome","device_type":"desktop"}
}
```

### Create Ticket
```bash
curl -sS -X POST "http://localhost:8080/tickets?autoClassify=true" \
  -H 'Content-Type: application/json' \
  -d '{
    "customer_id":"CUST-1",
    "customer_email":"user@example.com",
    "customer_name":"User",
    "subject":"Login issue",
    "description":"Cannot access account"
  }'
```

### List Tickets
```bash
curl -sS "http://localhost:8080/tickets?category=TECHNICAL_ISSUE&priority=URGENT"
```

### Get Ticket
```bash
curl -sS "http://localhost:8080/tickets/{id}"
```

### Update Ticket (partial)
```bash
curl -sS -X PUT "http://localhost:8080/tickets/{id}" \
  -H 'Content-Type: application/json' \
  -d '{"subject":"Updated subject"}'
```

### Delete Ticket
```bash
curl -sS -X DELETE "http://localhost:8080/tickets/{id}"
```

## Bulk Import
Endpoint: `POST /tickets/import`

- Multipart fields:
  - `file`: uploaded file
  - `format`: `csv` | `json` | `xml` (optional, auto-detected by extension)

Response (summary):
```json
{
  "totalRecords": 50,
  "successful": 45,
  "failed": 5,
  "failures": [{"index":2, "error":"customer_email: must be a well-formed email address"}]
}
```

Example:
```bash
curl -sS -X POST "http://localhost:8080/tickets/import" \
  -F "file=@sample_tickets.json" -F "format=json"
```

## Classification
Endpoint: `POST /tickets/{id}/auto-classify`

Response:
```json
{
  "category":"TECHNICAL_ISSUE",
  "priority":"URGENT",
  "confidence":0.95,
  "reasoning":"Category determined by keyword match; priority by urgency terms.",
  "keywords":["production down","crash"]
}
```
