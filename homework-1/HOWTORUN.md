# ▶️ How to Run the Application (macOS)

## Prerequisites
- JDK 21 installed (check with `java -version`)
- Git and VS Code

## Build & Run
```bash
cd homework-1
./gradlew clean build
./gradlew bootRun
```

- App: `http://localhost:3000`
- Swagger UI: `http://localhost:3000/swagger-ui/index.html`

## Test Requests
- Open `homework-1/demo/sample-requests.http` in VS Code and run the requests (Health, Create, List, Filters, Balance, Summary, Invalid).

Or use curl:
```bash
curl -X POST http://localhost:3000/transactions \
	-H "Content-Type: application/json" \
	-d '{
		"fromAccount": "ACC-12345",
		"toAccount": "ACC-67890",
		"amount": 100.50,
		"currency": "USD",
		"type": "transfer"
	}'

curl http://localhost:3000/transactions
curl http://localhost:3000/accounts/ACC-12345/balance
```

## Stop the App
- Press `Ctrl + C` in the terminal.

## Notes
- Uses in-memory storage; data resets on restart.
- Validation errors follow the schema in `TASKS.md`.