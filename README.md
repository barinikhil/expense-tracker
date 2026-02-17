# Expense Tracker Starter (Spring Boot + Angular)

This repository contains a basic full-stack starter setup:

- `backend/`: Spring Boot API (port `9081`)
- `frontend/`: Angular UI (port `4200`)
- `maven/`: Maven setup and backend build/run notes

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- npm 9+

## Steps to run the project

Open two terminals.

### Terminal 1: Start backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Available API endpoints:

- `GET /api/health`
- `GET /api/message`

Actuator endpoints:

- `GET /actuator/health`
- `GET /actuator/info`

Swagger/OpenAPI:

- Swagger UI: `http://localhost:9081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:9081/v3/api-docs`

### Terminal 2: Start frontend (Angular)

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200` in your browser.

## Optional: Build backend JAR with Maven

```bash
cd backend
./mvnw clean package
```

The packaged JAR will be created in `backend/target/`.


Maven Wrapper scripts will automatically download `maven-wrapper.jar` on first run.
