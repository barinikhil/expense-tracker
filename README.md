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
- `POST /api/auth/login`
- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `GET /api/sub-categories`
- `POST /api/sub-categories`
- `PUT /api/sub-categories/{id}`

Authentication details:

- Login/JWT guide: [backend/LOGIN.md](backend/LOGIN.md)

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


## Run with Docker Compose (split build and run)

Build artifacts first (reuses local Maven repository and `frontend/node_modules`):

```bash
docker compose --profile build up --abort-on-container-exit
```

Then run the prebuilt artifacts:

```bash
docker compose --profile run up
```

Services:

- UI (Nginx): `http://localhost:18080`
- Backend API: `http://localhost:10081`

The UI proxies API requests from `/api/*` to the backend service through Nginx.

To stop running containers:

```bash
docker compose --profile run down
```

## Optional: Build backend JAR with Maven

```bash
cd backend
./mvnw clean package
```

The packaged JAR will be created in `backend/target/`.


Maven Wrapper scripts will automatically download `maven-wrapper.jar` on first run.


## Category management feature

The app includes full category and sub-category management with database persistence.

- **List**: View all categories with nested sub-categories, and a global list of sub-categories.
- **Add**: Create new categories and sub-categories from the Angular UI.
- **Edit**: Update category details or move sub-categories to another category.
- **Database**: Spring Data JPA stores data in MySQL and auto-creates schema at startup.
