# Backend Login (JWT)

This backend uses database-based login and JWT authentication.

## Login Endpoint

- `POST /api/auth/login`

Request body:

```json
{
  "username": "u001",
  "password": "pass111"
}
```

Response:

```json
{
  "token": "<jwt_token>",
  "username": "u001"
}
```

## Seeded Users

On backend startup, users are seeded if they do not already exist:

- Username: `u001`, Password: `pass111`
- Username: `u002`, Password: `pass111`

## Use JWT Token

For protected API calls, send this header:

```http
Authorization: Bearer <jwt_token>
```

Protected endpoints include:

- `/api/categories`
- `/api/sub-categories`

Public endpoints include:

- `/api/auth/login`
- `/api/health`
