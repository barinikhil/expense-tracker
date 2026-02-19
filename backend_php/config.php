<?php

declare(strict_types=1);

function cfg_env(string $key, string $default): string
{
    $value = getenv($key);
    return $value === false || $value === '' ? $default : $value;
}

return [
    'db_host' => cfg_env('DB_HOST', '127.0.0.1'),
    'db_port' => cfg_env('DB_PORT', '3306'),
    'db_name' => cfg_env('DB_NAME', 'expense_tracker'),
    'db_user' => cfg_env('DB_USER', 'expense_tracker'),
    'db_pass' => cfg_env('DB_PASS', 'expense_tracker'),
    'jwt_secret' => cfg_env('JWT_SECRET', 'dev-only-change-this-secret-to-a-long-random-value'),
    'jwt_expiration_ms' => (int)cfg_env('JWT_EXPIRATION_MS', '86400000'),
    'cors_allowed_origins' => cfg_env('APP_CORS_ALLOWED_ORIGINS', '*'),
];

