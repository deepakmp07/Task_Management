# Task Management REST API

A production-ready RESTful API built with Java Spring Boot for managing users and tasks. This project demonstrates clean code practices, layered architecture, automated testing, and secure API design.

## 🚀 Key Features
- **User Management**: Create and retrieve users with email uniqueness constraints.
- **Task Lifecycle**: Full CRUD operations for tasks (Create, Read, Update, Delete).
- **Advanced Filtering**: Filter tasks by `status`, `priority`, and `assignedUser`.
- **Pagination**: Server-side pagination for optimized data loading.
- **Security**: Static API Key authentication using a custom security filter.
- **Data Integrity**: Database migrations handled via Flyway and schema validation.

---

## 🛠 Tech Stack
- **Backend**: Java 17+, Spring Boot 3.x
- **Data Access**: Spring Data JPA, Hibernate
- **Database**: PostgreSQL (Production), H2 (Testing)
- **Migrations**: Flyway
- **Tools**: Lombok, Maven, JUnit 5, Mockito
- **Documentation**: Standardized JSON Error Responses

---

## 🏃 Getting Started

### 1. Prerequisites
- JDK 17 or higher
- Maven 3.6+
- PostgreSQL server

### 2. Database Setup
Create a PostgreSQL database manually before starting the application:
```sql
CREATE DATABASE task_management;
```

### 3. Configuration
Update src/main/resources/application.properties with your credentials:

spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=your_username
spring.datasource.password=your_password

# Set your security key
api.key=interview-test-key

### 4. Running the App
# Compile and package
mvn clean install

# Run the application
mvn spring-boot:run

The API will be available at: http://localhost:8080
🔒 Security & Authentication
The API is protected by a static API key mechanism. Include the following header in every request:

Header KeyDescriptionExample Value
X-API-KEYStatic access keyinterview-test-key

📁 API Endpoints
User Endpoints
POST /api/users - Register a new user

GET /api/users - List all users (Paginated)

GET /api/users/{id} - Get a specific user by ID

Task Endpoints
POST /api/tasks - Create a new task

GET /api/tasks - Filtered & Paginated list (Params: status, priority, assignedToId)

GET /api/tasks/{id} - Get task details

PUT /api/tasks/{id} - Update a task fully

PATCH /api/tasks/{id}/status - Update only task status

DELETE /api/tasks/{id} - Remove a task

🧪 Testing & Quality
The project includes a comprehensive suite of tests to ensure reliability.

Running Tests
Bash

mvn test
Test Coverage
Unit Tests (src/test/java/.../service): Tests business logic in isolation using Mockito to mock repositories.

Integration Tests (src/test/java/.../controller): Tests full API flow from Controller to H2 Database using MockMvc.

🏗 Project Architecture
The project follows a standard Layered Architecture:

Controller Layer: Handles HTTP requests and input validation.

Service Layer: Contains business logic and transactional boundaries.

Repository Layer: Manages database communication via JPA.

DTO Layer: Decouples internal entities from API responses.
