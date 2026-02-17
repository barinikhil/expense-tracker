# Expense Tracker Starter (Spring Boot + Angular)

This repository now contains a basic full-stack starter setup:

- `backend/`: Spring Boot API (port `8080`)
- `frontend/`: Angular UI (port `4200`)

## Backend

```bash
cd backend
mvn spring-boot:run
```

Available endpoints:

- `GET /api/health`
- `GET /api/message`

## Frontend

```bash
cd frontend
npm install
npm start
```

The Angular UI calls the backend endpoints and displays connectivity status and a message.
