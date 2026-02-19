# backend_php

PHP replacement backend for the existing Java API.

## Run locally (PHP built-in server)

```bash
cd backend_php
php -S 0.0.0.0:9081 -t public public/index.php
```

## Environment variables

All environment reads are centralized in `backend_php/config.php`.

- `DB_HOST` (default `127.0.0.1`)
- `DB_PORT` (default `3306`)
- `DB_NAME` (default `expense_tracker`)
- `DB_USER` (default `expense_tracker`)
- `DB_PASS` (default `expense_tracker`)
- `JWT_SECRET` (required for secure usage; default dev-only fallback exists)
- `JWT_EXPIRATION_MS` (default `86400000`)
- `APP_CORS_ALLOWED_ORIGINS` (comma-separated, default `*`)

## Docker

```bash
docker build -t expense-tracker-backend-php ./backend_php
docker run --rm -p 10081:9081 \
  -e DB_HOST=192.168.7.210 \
  -e DB_PORT=3306 \
  -e DB_NAME=expense_tracker \
  -e DB_USER=expense_tracker \
  -e DB_PASS=expense_tracker \
  -e JWT_SECRET=change-me-to-a-long-random-secret \
  expense-tracker-backend-php
```

## Implemented API

- `GET /api/health`
- `POST /api/auth/login`
- `GET|POST|PUT /api/categories`
- `GET|POST|PUT /api/sub-categories`
- `GET|POST /api/transactions`
- `GET|PUT|DELETE /api/transactions/{id}`
- `GET|POST /api/expenses` (compat alias)
- `DELETE /api/expenses/{id}` (compat alias)
- `GET /api/dashboard/summary`
