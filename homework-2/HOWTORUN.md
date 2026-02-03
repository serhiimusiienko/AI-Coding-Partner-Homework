# How to Run

## Prerequisites
- Java 21

## Start the App
```bash
cd homework-2
./gradlew bootRun
```

## Demo Requests
```bash
bash homework-2/demo/requests.sh
```

## Import Sample Data
Create a simple JSON array and import:
```bash
cat > homework-2/sample_tickets.json <<'JSON'
[
  {"customer_id":"CUST-2","customer_email":"user2@example.com","customer_name":"User Two","subject":"Payment refund","description":"Requesting refund for last invoice"},
  {"customer_id":"CUST-3","customer_email":"user3@example.com","customer_name":"User Three","subject":"App crash","description":"App crashes on launch after update"}
]
JSON

curl -sS -X POST "http://localhost:8080/tickets/import" \
  -F "file=@homework-2/sample_tickets.json" -F "format=json"
```

## Run Tests & Coverage
```bash
cd homework-2
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```
