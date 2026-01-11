🗂 Task Management REST API

A production-ready RESTful API built with Java Spring Boot for managing users and tasks.
This project demonstrates clean code practices, layered architecture, database migrations, security, and automated testing.

🚀 Key Features

User Management

Create and retrieve users

Enforces unique email constraint

Task Lifecycle Management

Full CRUD operations (Create, Read, Update, Delete)

Advanced Filtering

Filter tasks by:

status

priority

assignedUser

Pagination

Server-side pagination for efficient data loading

Security

Static API Key authentication using a custom Spring Security filter

Data Integrity

Database schema migrations handled by Flyway

🛠 Tech Stack

Language: Java 17+

Framework: Spring Boot 3.x

Spring Web

Spring Data JPA

Spring Security

Spring Validation

Database:

PostgreSQL (Production)

H2 (Testing)

Migration Tool: Flyway

Build Tool: Maven

Testing:

JUnit 5

Mockito

Utilities:

Lombok

🏃 Getting Started
1️⃣ Prerequisites

Ensure you have the following installed:

JDK 17 or higher

Maven 3.6+

PostgreSQL Server

2️⃣ Database Setup

Login to PostgreSQL and create the database:

CREATE DATABASE task_management;

3️⃣ Application Configuration

Update the file:

src/main/resources/application.properties

spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=your_username
spring.datasource.password=your_password

# API Authentication Key
api.key=test-api-key

4️⃣ Running the Application
# Compile and package the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run


The API will be available at:

http://localhost:8080

🔒 Security & Authentication

This API is protected using a static API key.

Required Request Header
Header Key	Description	Example
X-API-KEY	Static API access key	test-api-key

⚠️ Every API request must include this header, otherwise you will receive a 401 Unauthorized response.

📁 API Endpoints
👤 User Endpoints
Method	Endpoint	Description
POST	/api/users	Register a new user
GET	/api/users	Get all users (Paginated)
GET	/api/users/{id}	Get user by ID
✅ Task Endpoints
Method	Endpoint	Description
POST	/api/tasks	Create a new task
GET	/api/tasks	Get tasks (Filter & Pagination)
GET	/api/tasks/{id}	Get task by ID
PUT	/api/tasks/{id}	Update a task
PATCH	/api/tasks/{id}/status	Update task status
DELETE	/api/tasks/{id}	Delete a task

Filtering Parameters:

status

priority

assignedToId

🧪 Testing & Quality

The project includes a comprehensive test suite to ensure reliability and correctness.

Running Tests
mvn test

Test Coverage

Unit Tests

Located in src/test/java/.../service

Tests business logic using Mockito

Integration Tests

Located in src/test/java/.../controller

Tests full API flow using MockMvc and H2

🏗 Project Architecture

The application follows a Layered Architecture:

Controller Layer

Handles HTTP requests and validation

Service Layer

Contains business logic and transactions

Repository Layer

Manages database access using Spring Data JPA

DTO Layer

Separates API models from database entities

✅ Status

✔ Production-ready
✔ Secure
✔ Tested
✔ Clean architecture
