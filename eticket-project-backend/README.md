# eTicket Backend

Basic Spring Boot backend with JWT authentication.

## Endpoints

- `POST /register` creates a user and returns a JWT.
- `POST /login` verifies credentials and returns a JWT.
- All other endpoints require `Authorization: Bearer <token>`.

## Quick Start

```powershell
.\mvnw test
.\mvnw spring-boot:run
```

## Example Requests

```text
POST /register
{
  "email": "user@example.com",
  "password": "secret",
  "firstName": "Ula",
  "lastName": "User"
}

Response
{
  "token": "<jwt>",
  "id": "<uuid>",
  "email": "user@example.com",
  "role": "PASSENGER",
  "firstName": "Ula",
  "lastName": "User"
}
```

```text
GET /tickets
Authorization: Bearer <jwt>
```
