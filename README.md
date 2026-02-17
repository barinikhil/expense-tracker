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
- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `GET /api/sub-categories`
- `POST /api/sub-categories`
- `PUT /api/sub-categories/{id}`

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


## Category management feature

The app now includes full category and sub-category management with database persistence using H2.

- **List**: View all categories with nested sub-categories, and a global list of sub-categories.
- **Add**: Create new categories and sub-categories from the Angular UI.
- **Edit**: Update category details or move sub-categories to another category.
- **Database**: Spring Data JPA stores data in H2 (`jdbc:h2:mem:expensetracker`) and auto-creates schema at startup.
