# Task Management REST API

A REST API for managing tasks and users built with Spring Boot and PostgreSQL.

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 14+

## Quick Setup

### 1. Create Database

```bash
psql -U postgres
CREATE DATABASE task_management;
\q
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

api.key=your-secret-api-key-12345
```

### 3. Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Application will start at: **http://localhost:8080**

## Testing the API

### Using Postman

1. **Set Header** for all requests:
   ```
   X-API-KEY: your-secret-api-key-12345
   ```

2. **Create a User**:
   ```
   POST http://localhost:8080/api/users
   Content-Type: application/json
   
   {
     "name": "John Doe",
     "email": "john@example.com"
   }
   ```

3. **Create a Task**:
   ```
   POST http://localhost:8080/api/tasks
   Content-Type: application/json
   
   {
     "title": "Complete project",
     "description": "Finish the API",
     "status": "TODO",
     "priority": "HIGH",
     "assignedToId": 1
   }
   ```

4. **Get All Tasks**:
   ```
   GET http://localhost:8080/api/tasks
   ```

## API Endpoints

### Users
- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID

### Tasks
- `POST /api/tasks` - Create task
- `GET /api/tasks` - Get all tasks (supports filtering)
- `GET /api/tasks/{id}` - Get task by ID
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Delete task

### Filters (for GET /api/tasks)
- `?status=TODO` - Filter by status (TODO, IN_PROGRESS, DONE)
- `?priority=HIGH` - Filter by priority (LOW, MEDIUM, HIGH)
- `?assignedToId=1` - Filter by assigned user
- `?page=0&size=10` - Pagination

## Run Tests

```bash
mvn test
```

## Project Structure

```
src/
├── main/java/com/example/taskmanagement/
│   ├── controller/     # REST endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Database access
│   ├── entity/         # Database models
│   └── dto/            # Request/Response objects
└── test/               # Unit & Integration tests
```

## Tech Stack

- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Flyway (Database migrations)
- Lombok
- JUnit 5 & Mockito (Testing)

---

**That's it! You're ready to go.** 🚀
