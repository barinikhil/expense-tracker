# Maven Setup

This folder documents how Maven is used in this project.

## Prerequisites

- Java 17+
- Maven 3.9+

## Run backend with Maven

From the project root:

```bash
cd backend
./mvnw spring-boot:run
```

## Build backend JAR

```bash
cd backend
./mvnw clean package
```

The output JAR is generated in `backend/target/`.


Maven Wrapper scripts will automatically download `maven-wrapper.jar` on first run.


## API documentation and monitoring

After starting the backend, use:

- Actuator health: `http://localhost:9081/actuator/health`
- Actuator info: `http://localhost:9081/actuator/info`
- Swagger UI: `http://localhost:9081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:9081/v3/api-docs`
