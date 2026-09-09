# Task Management API

## Overview

A Spring Boot REST API for managing users, projects, and tasks. Users authenticate with JWT bearer tokens, manage their own projects and tasks, and administrators can manage users.

## Features

- User signup and login with BCrypt password hashing
- Stateless JWT authentication
- USER and ADMIN roles
- Project and task CRUD operations
- Ownership checks for projects and tasks
- Task pagination, sorting, and filtering
- Request validation and consistent API errors
- OpenAPI/Swagger UI documentation

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- H2 for tests
- Maven
- JJWT

## Architecture

The application is organized into:

- `controller`: HTTP endpoints and request binding
- `service`: business rules, ownership checks, and authentication
- `repository`: Spring Data persistence interfaces
- `model`: JPA entities and enums
- `dto`: request and response records
- `security`: JWT processing and Spring Security configuration
- `exception`: standard API error handling
- `config`: OpenAPI configuration

## Database Relationships

```text
User 1 ---- * Project 1 ---- * Task
```

- Each project belongs to one user.
- Each task belongs to one project.
- Deleting a user cascades to projects and tasks.
- Deleting a project cascades to its tasks.
- Project and task relationships use lazy many-to-one loading.

## Authentication Flow

1. Sign up with `POST /api/auth/signup`.
2. Log in with `POST /api/auth/login`.
3. Send the returned token in the `Authorization` header:

```text
Authorization: Bearer <jwt>
```

The JWT contains the username, role, issued-at time, and expiration time. Access tokens are stateless; there is no refresh-token endpoint.

## Authorization Rules

- Signup and login are public.
- Authenticated users can manage their own projects and tasks.
- A user cannot read or modify another user's projects or tasks.
- Only ADMIN users can list, view, or delete users.
- Missing credentials or invalid JWTs return `401 Unauthorized`.
- Authenticated users without sufficient permission return `403 Forbidden`.

## API Endpoints

### Authentication

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/signup` | Create a USER account |
| POST | `/api/auth/login` | Authenticate and receive a JWT |

### Users

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/users/me` | Get the authenticated user's profile |
| PUT | `/api/users/me` | Update the authenticated user's email/password |
| GET | `/api/users` | List users (ADMIN only) |
| GET | `/api/users/{id}` | Get a user (ADMIN only) |
| DELETE | `/api/users/{id}` | Delete a user (ADMIN only) |

### Projects

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/projects` | List the authenticated user's projects |
| GET | `/api/projects/{id}` | Get an owned project |
| POST | `/api/projects` | Create a project |
| PUT | `/api/projects/{id}` | Update an owned project |
| DELETE | `/api/projects/{id}` | Delete an owned project |

### Tasks

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/tasks` | List owned tasks with pagination/filtering/sorting |
| GET | `/api/tasks/{id}` | Get an owned task |
| POST | `/api/tasks/projects/{projectId}` | Create a task in an owned project |
| PUT | `/api/tasks/{id}` | Update an owned task |
| DELETE | `/api/tasks/{id}` | Delete an owned task |

Task list query parameters include `page`, `size`, `sort`, `direction`, `projectId`, `status`, `priority`, `dueDate`, `dueDateFrom`, and `dueDateTo`.

## Environment Variables

The application requires:

| Variable | Purpose |
| --- | --- |
| `APP_JWT_SECRET` | Strong JWT signing secret, at least 32 characters |
| `DB_USERNAME` | MySQL username; defaults to `root` locally |
| `DB_PASSWORD` | MySQL password |

Optional variables:

| Variable | Purpose |
| --- | --- |
| `APP_JWT_EXPIRATION_MS` | JWT lifetime in milliseconds; defaults to 86400000 |
| `SHOW_SQL` | Enable SQL logging locally; defaults to `false` |

## MySQL Setup

Create the database:

```sql
CREATE DATABASE task_management_db;
```

Set `DB_USERNAME`, `DB_PASSWORD`, and `APP_JWT_SECRET` in the environment before starting the application. The default local profile uses Hibernate `ddl-auto=update`. For production-oriented startup, use the `prod` profile, which uses `ddl-auto=validate`.

## How to Run

From the project directory:

```powershell
$env:APP_JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-characters"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-mysql-password"
.\mvnw.cmd spring-boot:run
```

For the production-oriented JPA settings:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
.\mvnw.cmd spring-boot:run
```

## How to Run Tests

Tests use an in-memory H2 database and the `test` profile:

```powershell
.\mvnw.cmd test
```

## Swagger

With the application running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Example API Flow

Sign up:

```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123"
}
```

Log in:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "Password123"
}
```

Create a project with the returned token:

```http
POST /api/projects
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "Portfolio project",
  "description": "My project",
  "status": "ACTIVE"
}
```

Create a task:

```http
POST /api/tasks/projects/{projectId}
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "title": "Write documentation",
  "description": "Document the API",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2030-01-10"
}
```

## Project Structure

```text
src/
  main/
    java/com/example/task_management_api/
      config/
      controller/
      dto/
      exception/
      model/
      repository/
      security/
      service/
    resources/
      application.properties
      application-prod.properties
  test/
    java/
    resources/application-test.properties
```
