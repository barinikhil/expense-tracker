# Expense Tracker Starter (Spring Boot + Angular)

This repository contains a basic full-stack starter setup:

- `backend/`: Spring Boot API (port `8080`)
- `frontend/`: Angular UI (port `4200`)

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+ and npm

## Run on Windows (PowerShell)

Open **two PowerShell terminals**.

### 1) Start the Spring Boot backend

```powershell
cd backend
mvn spring-boot:run
```

Backend endpoints:

- `GET http://localhost:8080/api/health`
- `GET http://localhost:8080/api/message`

### 2) Start the Angular frontend

```powershell
cd frontend
npm install
npm start
```

Then open:

- `http://localhost:4200`

The Angular UI calls the backend and displays connectivity status and message.

## Run on macOS/Linux

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```
