<?php

declare(strict_types=1);

header('Content-Type: application/json');

$config = require __DIR__ . '/../config.php';

function json_input(): array
{
    $raw = file_get_contents('php://input');
    if ($raw === false || trim($raw) === '') {
        return [];
    }
    $decoded = json_decode($raw, true);
    return is_array($decoded) ? $decoded : [];
}

function send_json(int $status, array $payload = []): never
{
    http_response_code($status);
    if ($status !== 204) {
        echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    }
    exit;
}

function b64url_encode(string $value): string
{
    return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
}

function b64url_decode(string $value): string
{
    $padLen = 4 - (strlen($value) % 4);
    if ($padLen < 4) {
        $value .= str_repeat('=', $padLen);
    }
    return (string)base64_decode(strtr($value, '-_', '+/'));
}

function sign_jwt(string $username, string $secret, int $expirationMs): string
{
    $header = ['alg' => 'HS256', 'typ' => 'JWT'];
    $now = time();
    $payload = ['sub' => $username, 'iat' => $now, 'exp' => $now + intdiv($expirationMs, 1000)];

    $h = b64url_encode(json_encode($header, JSON_UNESCAPED_UNICODE));
    $p = b64url_encode(json_encode($payload, JSON_UNESCAPED_UNICODE));
    $sig = hash_hmac('sha256', $h . '.' . $p, $secret, true);
    return $h . '.' . $p . '.' . b64url_encode($sig);
}

function verify_jwt(string $jwt, string $secret): ?string
{
    $parts = explode('.', $jwt);
    if (count($parts) !== 3) {
        return null;
    }

    [$h, $p, $s] = $parts;
    $expected = b64url_encode(hash_hmac('sha256', $h . '.' . $p, $secret, true));
    if (!hash_equals($expected, $s)) {
        return null;
    }

    $payload = json_decode(b64url_decode($p), true);
    if (!is_array($payload) || !isset($payload['sub'], $payload['exp'])) {
        return null;
    }
    if ((int)$payload['exp'] < time()) {
        return null;
    }
    return (string)$payload['sub'];
}

function to_decimal(float $value): string
{
    return number_format($value, 2, '.', '');
}

function parse_date(?string $value): ?string
{
    if ($value === null || $value === '') {
        return null;
    }
    $dt = DateTimeImmutable::createFromFormat('Y-m-d', $value);
    return $dt && $dt->format('Y-m-d') === $value ? $value : null;
}

function authorization_header(): string
{
    $direct = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (is_string($direct) && $direct !== '') {
        return $direct;
    }

    if (function_exists('apache_request_headers')) {
        $headers = apache_request_headers();
        foreach ($headers as $key => $value) {
            if (strcasecmp((string)$key, 'Authorization') === 0) {
                return is_string($value) ? $value : '';
            }
        }
    }

    return '';
}

function month_key(DateTimeImmutable $dt): string
{
    return $dt->format('Y-m');
}

function between_dates(string $target, string $from, string $to): bool
{
    return $target >= $from && $target <= $to;
}

$allowedOrigins = array_map('trim', explode(',', (string)$config['cors_allowed_origins']));
$origin = $_SERVER['HTTP_ORIGIN'] ?? null;
if ($origin !== null) {
    if (in_array('*', $allowedOrigins, true) || in_array($origin, $allowedOrigins, true)) {
        header('Access-Control-Allow-Origin: ' . (in_array('*', $allowedOrigins, true) ? '*' : $origin));
        header('Vary: Origin');
    } else {
        send_json(403, ['message' => 'Invalid CORS request']);
    }
}
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    send_json(204);
}

$dsn = sprintf(
    'mysql:host=%s;port=%s;dbname=%s;charset=utf8mb4',
    (string)$config['db_host'],
    (string)$config['db_port'],
    (string)$config['db_name']
);

try {
    $pdo = new PDO(
        $dsn,
        (string)$config['db_user'],
        (string)$config['db_pass'],
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
    );
} catch (Throwable $e) {
    send_json(500, ['message' => 'Database connection failed']);
}

$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';
$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$jwtSecret = (string)$config['jwt_secret'];
$jwtTtlMs = (int)$config['jwt_expiration_ms'];

$publicRoutes = [
    ['GET', '#^/api/health/?$#'],
    ['HEAD', '#^/api/health/?$#'],
    ['POST', '#^/api/auth/login/?$#'],
];
$isPublic = false;
foreach ($publicRoutes as [$m, $pattern]) {
    if ($method === $m && preg_match($pattern, $path) === 1) {
        $isPublic = true;
        break;
    }
}

$currentUser = null;
if (!$isPublic && str_starts_with($path, '/api/')) {
    $auth = authorization_header();
    if (!str_starts_with($auth, 'Bearer ')) {
        send_json(401, ['message' => 'Unauthorized']);
    }
    $token = substr($auth, 7);
    $currentUser = verify_jwt($token, $jwtSecret);
    if ($currentUser === null) {
        send_json(401, ['message' => 'Unauthorized']);
    }
}

if ($method === 'GET' && preg_match('#^/api/health/?$#', $path) === 1) {
    send_json(200, ['status' => 'UP']);
}

if ($method === 'POST' && preg_match('#^/api/auth/login/?$#', $path) === 1) {
    $input = json_input();
    $username = trim((string)($input['username'] ?? ''));
    $password = (string)($input['password'] ?? '');
    if ($username === '' || $password === '') {
        send_json(400, ['message' => 'username and password are required']);
    }

    $stmt = $pdo->prepare('SELECT username, password_hash, active FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1');
    $stmt->execute(['username' => $username]);
    $user = $stmt->fetch();
    if (!$user || !(bool)$user['active'] || !password_verify($password, (string)$user['password_hash'])) {
        send_json(401, ['message' => 'Invalid username or password']);
    }

    $token = sign_jwt((string)$user['username'], $jwtSecret, $jwtTtlMs);
    send_json(200, ['token' => $token, 'username' => (string)$user['username']]);
}

if ($method === 'GET' && $path === '/api/categories') {
    $cats = $pdo->query('SELECT id, name, description, category_type FROM categories ORDER BY name')->fetchAll();
    $subs = $pdo->query('SELECT id, name, category_id FROM sub_categories ORDER BY name')->fetchAll();
    $subsByCategory = [];
    foreach ($subs as $s) {
        $cid = (int)$s['category_id'];
        $subsByCategory[$cid][] = [
            'id' => (int)$s['id'],
            'name' => (string)$s['name'],
            'categoryId' => $cid,
            'categoryName' => null,
        ];
    }

    $out = [];
    foreach ($cats as $c) {
        $cid = (int)$c['id'];
        $items = $subsByCategory[$cid] ?? [];
        foreach ($items as &$item) {
            $item['categoryName'] = (string)$c['name'];
        }
        $out[] = [
            'id' => $cid,
            'name' => (string)$c['name'],
            'description' => (string)$c['description'],
            'type' => (string)$c['category_type'],
            'subCategories' => $items,
        ];
    }
    send_json(200, $out);
}

if ($method === 'POST' && $path === '/api/categories') {
    $input = json_input();
    $name = trim((string)($input['name'] ?? ''));
    $description = trim((string)($input['description'] ?? ''));
    $type = strtoupper(trim((string)($input['type'] ?? 'EXPENSE')));
    if ($name === '' || $description === '' || !in_array($type, ['EXPENSE', 'INCOME', 'SAVING'], true)) {
        send_json(400, ['message' => 'Invalid category payload']);
    }
    $stmt = $pdo->prepare('INSERT INTO categories(name, description, category_type, created_by, created_on) VALUES(:name, :description, :type, :createdBy, NOW(6))');
    try {
        $stmt->execute(['name' => $name, 'description' => $description, 'type' => $type, 'createdBy' => $currentUser ?? 'system']);
    } catch (Throwable $e) {
        send_json(409, ['message' => 'Category already exists']);
    }
    $id = (int)$pdo->lastInsertId();
    send_json(201, ['id' => $id, 'name' => $name, 'description' => $description, 'type' => $type, 'subCategories' => []]);
}

if ($method === 'PUT' && preg_match('#^/api/categories/(\d+)$#', $path, $m) === 1) {
    $id = (int)$m[1];
    $input = json_input();
    $name = trim((string)($input['name'] ?? ''));
    $description = trim((string)($input['description'] ?? ''));
    $type = strtoupper(trim((string)($input['type'] ?? 'EXPENSE')));
    if ($name === '' || $description === '' || !in_array($type, ['EXPENSE', 'INCOME', 'SAVING'], true)) {
        send_json(400, ['message' => 'Invalid category payload']);
    }
    $stmt = $pdo->prepare('UPDATE categories SET name=:name, description=:description, category_type=:type, updated_by=:updatedBy, updated_on=NOW(6) WHERE id=:id');
    $stmt->execute(['name' => $name, 'description' => $description, 'type' => $type, 'updatedBy' => $currentUser ?? 'system', 'id' => $id]);
    if ($stmt->rowCount() === 0) {
        send_json(404, ['message' => 'Category not found']);
    }
    send_json(200, ['id' => $id, 'name' => $name, 'description' => $description, 'type' => $type, 'subCategories' => []]);
}

if ($method === 'GET' && $path === '/api/sub-categories') {
    $sql = 'SELECT s.id, s.name, s.category_id, c.name AS category_name FROM sub_categories s JOIN categories c ON c.id = s.category_id ORDER BY c.name, s.name';
    $rows = $pdo->query($sql)->fetchAll();
    $out = array_map(static fn(array $r): array => [
        'id' => (int)$r['id'],
        'name' => (string)$r['name'],
        'categoryId' => (int)$r['category_id'],
        'categoryName' => (string)$r['category_name'],
    ], $rows);
    send_json(200, $out);
}

if ($method === 'POST' && $path === '/api/sub-categories') {
    $input = json_input();
    $name = trim((string)($input['name'] ?? ''));
    $categoryId = (int)($input['categoryId'] ?? 0);
    if ($name === '' || $categoryId <= 0) {
        send_json(400, ['message' => 'Invalid sub-category payload']);
    }
    $exists = $pdo->prepare('SELECT id, name FROM categories WHERE id=:id');
    $exists->execute(['id' => $categoryId]);
    $category = $exists->fetch();
    if (!$category) {
        send_json(404, ['message' => 'Category not found']);
    }
    $stmt = $pdo->prepare('INSERT INTO sub_categories(name, category_id, created_by, created_on) VALUES(:name, :categoryId, :createdBy, NOW(6))');
    $stmt->execute(['name' => $name, 'categoryId' => $categoryId, 'createdBy' => $currentUser ?? 'system']);
    $id = (int)$pdo->lastInsertId();
    send_json(201, ['id' => $id, 'name' => $name, 'categoryId' => $categoryId, 'categoryName' => (string)$category['name']]);
}

if ($method === 'PUT' && preg_match('#^/api/sub-categories/(\d+)$#', $path, $m) === 1) {
    $id = (int)$m[1];
    $input = json_input();
    $name = trim((string)($input['name'] ?? ''));
    $categoryId = (int)($input['categoryId'] ?? 0);
    if ($name === '' || $categoryId <= 0) {
        send_json(400, ['message' => 'Invalid sub-category payload']);
    }
    $exists = $pdo->prepare('SELECT id, name FROM categories WHERE id=:id');
    $exists->execute(['id' => $categoryId]);
    $category = $exists->fetch();
    if (!$category) {
        send_json(404, ['message' => 'Category not found']);
    }
    $stmt = $pdo->prepare('UPDATE sub_categories SET name=:name, category_id=:categoryId, updated_by=:updatedBy, updated_on=NOW(6) WHERE id=:id');
    $stmt->execute(['name' => $name, 'categoryId' => $categoryId, 'updatedBy' => $currentUser ?? 'system', 'id' => $id]);
    if ($stmt->rowCount() === 0) {
        send_json(404, ['message' => 'Sub-category not found']);
    }
    send_json(200, ['id' => $id, 'name' => $name, 'categoryId' => $categoryId, 'categoryName' => (string)$category['name']]);
}

if ($method === 'GET' && $path === '/api/expenses') {
    $startDate = parse_date($_GET['startDate'] ?? null);
    $endDate = parse_date($_GET['endDate'] ?? null);
    $page = max(0, (int)($_GET['page'] ?? 0));
    $size = (int)($_GET['size'] ?? 10);
    if ($size <= 0 || $size > 200) {
        send_json(400, ['message' => 'size must be between 1 and 200']);
    }
    if ($startDate !== null && $endDate !== null && $startDate > $endDate) {
        send_json(400, ['message' => 'startDate cannot be after endDate']);
    }

    $where = [];
    $params = [];
    if ($startDate !== null) {
        $where[] = 'e.expense_date >= :startDate';
        $params['startDate'] = $startDate;
    }
    if ($endDate !== null) {
        $where[] = 'e.expense_date <= :endDate';
        $params['endDate'] = $endDate;
    }
    $whereSql = count($where) > 0 ? ('WHERE ' . implode(' AND ', $where)) : '';

    $countSql = "SELECT COUNT(*) FROM expenses e $whereSql";
    $countStmt = $pdo->prepare($countSql);
    $countStmt->execute($params);
    $totalElements = (int)$countStmt->fetchColumn();
    $totalPages = $totalElements === 0 ? 0 : (int)ceil($totalElements / $size);

    $offset = $page * $size;
    $sql = "SELECT e.id, e.amount, e.description, e.expense_date, e.transaction_type, e.category_id, c.name AS category_name, e.sub_category_id, s.name AS sub_category_name
            FROM expenses e
            JOIN categories c ON c.id = e.category_id
            JOIN sub_categories s ON s.id = e.sub_category_id
            $whereSql
            ORDER BY e.expense_date DESC, e.id DESC
            LIMIT :limit OFFSET :offset";
    $stmt = $pdo->prepare($sql);
    foreach ($params as $k => $v) {
        $stmt->bindValue(':' . $k, $v);
    }
    $stmt->bindValue(':limit', $size, PDO::PARAM_INT);
    $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
    $stmt->execute();
    $rows = $stmt->fetchAll();

    $items = array_map(static fn(array $r): array => [
        'id' => (int)$r['id'],
        'amount' => (float)$r['amount'],
        'description' => (string)$r['description'],
        'expenseDate' => (string)$r['expense_date'],
        'type' => (string)$r['transaction_type'],
        'categoryId' => (int)$r['category_id'],
        'categoryName' => (string)$r['category_name'],
        'subCategoryId' => (int)$r['sub_category_id'],
        'subCategoryName' => (string)$r['sub_category_name'],
    ], $rows);

    send_json(200, [
        'items' => $items,
        'page' => $page,
        'size' => $size,
        'totalElements' => $totalElements,
        'totalPages' => $totalPages,
    ]);
}

if ($method === 'GET' && $path === '/api/transactions') {
    $type = strtoupper((string)($_GET['type'] ?? 'EXPENSE'));
    $startDate = parse_date($_GET['startDate'] ?? null);
    $endDate = parse_date($_GET['endDate'] ?? null);
    $categoryId = isset($_GET['categoryId']) ? (int)$_GET['categoryId'] : null;
    $subCategoryId = isset($_GET['subCategoryId']) ? (int)$_GET['subCategoryId'] : null;
    $minAmount = isset($_GET['minAmount']) ? (float)$_GET['minAmount'] : null;
    $maxAmount = isset($_GET['maxAmount']) ? (float)$_GET['maxAmount'] : null;
    $sortBy = (string)($_GET['sortBy'] ?? 'expenseDate');
    $sortDir = strtolower((string)($_GET['sortDir'] ?? 'desc')) === 'asc' ? 'asc' : 'desc';
    $page = max(0, (int)($_GET['page'] ?? 0));
    $size = (int)($_GET['size'] ?? 10);
    if ($size <= 0 || $size > 200) {
        send_json(400, ['message' => 'size must be between 1 and 200']);
    }
    if (!in_array($type, ['EXPENSE', 'INCOME'], true)) {
        send_json(400, ['message' => 'type must be EXPENSE or INCOME']);
    }

    $sortMap = [
        'expenseDate' => 'e.expense_date',
        'amount' => 'e.amount',
        'category' => 'c.name',
        'subCategory' => 's.name',
    ];
    if (!isset($sortMap[$sortBy])) {
        send_json(400, ['message' => 'sortBy must be one of: expenseDate, amount, category, subCategory']);
    }

    $where = ['e.transaction_type = :type'];
    $params = ['type' => $type];
    if ($startDate !== null) {
        $where[] = 'e.expense_date >= :startDate';
        $params['startDate'] = $startDate;
    }
    if ($endDate !== null) {
        $where[] = 'e.expense_date <= :endDate';
        $params['endDate'] = $endDate;
    }
    if ($categoryId !== null && $categoryId > 0) {
        $where[] = 'e.category_id = :categoryId';
        $params['categoryId'] = $categoryId;
    }
    if ($subCategoryId !== null && $subCategoryId > 0) {
        $where[] = 'e.sub_category_id = :subCategoryId';
        $params['subCategoryId'] = $subCategoryId;
    }
    if ($minAmount !== null) {
        $where[] = 'e.amount >= :minAmount';
        $params['minAmount'] = $minAmount;
    }
    if ($maxAmount !== null) {
        $where[] = 'e.amount <= :maxAmount';
        $params['maxAmount'] = $maxAmount;
    }

    $whereSql = implode(' AND ', $where);
    $countSql = "SELECT COUNT(*) FROM expenses e WHERE $whereSql";
    $countStmt = $pdo->prepare($countSql);
    $countStmt->execute($params);
    $totalElements = (int)$countStmt->fetchColumn();
    $totalPages = $totalElements === 0 ? 0 : (int)ceil($totalElements / $size);

    $offset = $page * $size;
    $order = $sortMap[$sortBy] . ' ' . strtoupper($sortDir) . ', e.expense_date DESC, e.id DESC';
    $sql = "SELECT e.id, e.amount, e.description, e.expense_date, e.transaction_type, e.category_id, c.name AS category_name, e.sub_category_id, s.name AS sub_category_name
            FROM expenses e
            JOIN categories c ON c.id = e.category_id
            JOIN sub_categories s ON s.id = e.sub_category_id
            WHERE $whereSql
            ORDER BY $order
            LIMIT :limit OFFSET :offset";
    $stmt = $pdo->prepare($sql);
    foreach ($params as $k => $v) {
        $stmt->bindValue(':' . $k, $v);
    }
    $stmt->bindValue(':limit', $size, PDO::PARAM_INT);
    $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
    $stmt->execute();
    $rows = $stmt->fetchAll();

    $items = array_map(static fn(array $r): array => [
        'id' => (int)$r['id'],
        'amount' => (float)$r['amount'],
        'description' => (string)$r['description'],
        'expenseDate' => (string)$r['expense_date'],
        'type' => (string)$r['transaction_type'],
        'categoryId' => (int)$r['category_id'],
        'categoryName' => (string)$r['category_name'],
        'subCategoryId' => (int)$r['sub_category_id'],
        'subCategoryName' => (string)$r['sub_category_name'],
    ], $rows);

    send_json(200, [
        'items' => $items,
        'page' => $page,
        'size' => $size,
        'totalElements' => $totalElements,
        'totalPages' => $totalPages,
    ]);
}

if (($method === 'POST' && $path === '/api/transactions') || ($method === 'POST' && $path === '/api/expenses')) {
    $input = json_input();
    $amount = (float)($input['amount'] ?? 0);
    $description = trim((string)($input['description'] ?? ''));
    $expenseDate = parse_date($input['expenseDate'] ?? null);
    $type = strtoupper(trim((string)($input['type'] ?? ($path === '/api/expenses' ? 'EXPENSE' : 'EXPENSE'))));
    $categoryId = (int)($input['categoryId'] ?? 0);
    $subCategoryId = (int)($input['subCategoryId'] ?? 0);

    if ($amount <= 0 || $description === '' || $expenseDate === null || $categoryId <= 0 || $subCategoryId <= 0 || !in_array($type, ['EXPENSE', 'INCOME'], true)) {
        send_json(400, ['message' => 'Invalid transaction payload']);
    }

    $catStmt = $pdo->prepare('SELECT id, name, category_type FROM categories WHERE id=:id');
    $catStmt->execute(['id' => $categoryId]);
    $category = $catStmt->fetch();
    if (!$category) {
        send_json(404, ['message' => 'Category not found']);
    }
    $subStmt = $pdo->prepare('SELECT id, name, category_id FROM sub_categories WHERE id=:id');
    $subStmt->execute(['id' => $subCategoryId]);
    $subCategory = $subStmt->fetch();
    if (!$subCategory) {
        send_json(404, ['message' => 'Sub-category not found']);
    }
    if ((int)$subCategory['category_id'] !== $categoryId) {
        send_json(400, ['message' => 'Sub-category does not belong to selected category']);
    }
    if ($type === 'INCOME' && $category['category_type'] !== 'INCOME') {
        send_json(400, ['message' => 'Category type must be INCOME for income transactions']);
    }
    if ($type === 'EXPENSE' && !in_array($category['category_type'], ['EXPENSE', 'SAVING'], true)) {
        send_json(400, ['message' => 'Category type must be EXPENSE or SAVING for expense transactions']);
    }

    $stmt = $pdo->prepare('INSERT INTO expenses(amount, description, expense_date, transaction_type, category_id, sub_category_id, created_by, created_on)
                           VALUES(:amount, :description, :expenseDate, :type, :categoryId, :subCategoryId, :createdBy, NOW(6))');
    $stmt->execute([
        'amount' => $amount,
        'description' => $description,
        'expenseDate' => $expenseDate,
        'type' => $type,
        'categoryId' => $categoryId,
        'subCategoryId' => $subCategoryId,
        'createdBy' => $currentUser ?? 'system',
    ]);
    $id = (int)$pdo->lastInsertId();
    send_json(201, [
        'id' => $id,
        'amount' => $amount,
        'description' => $description,
        'expenseDate' => $expenseDate,
        'type' => $type,
        'categoryId' => $categoryId,
        'categoryName' => (string)$category['name'],
        'subCategoryId' => $subCategoryId,
        'subCategoryName' => (string)$subCategory['name'],
    ]);
}

if ($method === 'GET' && preg_match('#^/api/transactions/(\d+)$#', $path, $m) === 1) {
    $id = (int)$m[1];
    $sql = 'SELECT e.id, e.amount, e.description, e.expense_date, e.transaction_type, e.category_id, c.name AS category_name, e.sub_category_id, s.name AS sub_category_name
            FROM expenses e
            JOIN categories c ON c.id = e.category_id
            JOIN sub_categories s ON s.id = e.sub_category_id
            WHERE e.id = :id';
    $stmt = $pdo->prepare($sql);
    $stmt->execute(['id' => $id]);
    $r = $stmt->fetch();
    if (!$r) {
        send_json(404, ['message' => 'Transaction not found']);
    }
    send_json(200, [
        'id' => (int)$r['id'],
        'amount' => (float)$r['amount'],
        'description' => (string)$r['description'],
        'expenseDate' => (string)$r['expense_date'],
        'type' => (string)$r['transaction_type'],
        'categoryId' => (int)$r['category_id'],
        'categoryName' => (string)$r['category_name'],
        'subCategoryId' => (int)$r['sub_category_id'],
        'subCategoryName' => (string)$r['sub_category_name'],
    ]);
}

if ($method === 'PUT' && preg_match('#^/api/transactions/(\d+)$#', $path, $m) === 1) {
    $id = (int)$m[1];
    $oldStmt = $pdo->prepare('SELECT id, transaction_type FROM expenses WHERE id=:id');
    $oldStmt->execute(['id' => $id]);
    $existing = $oldStmt->fetch();
    if (!$existing) {
        send_json(404, ['message' => 'Transaction not found']);
    }

    $input = json_input();
    $amount = (float)($input['amount'] ?? 0);
    $description = trim((string)($input['description'] ?? ''));
    $expenseDate = parse_date($input['expenseDate'] ?? null);
    $type = strtoupper(trim((string)($input['type'] ?? 'EXPENSE')));
    $categoryId = (int)($input['categoryId'] ?? 0);
    $subCategoryId = (int)($input['subCategoryId'] ?? 0);

    if ($amount <= 0 || $description === '' || $expenseDate === null || $categoryId <= 0 || $subCategoryId <= 0 || !in_array($type, ['EXPENSE', 'INCOME'], true)) {
        send_json(400, ['message' => 'Invalid transaction payload']);
    }
    if ((string)$existing['transaction_type'] !== $type) {
        send_json(400, ['message' => 'Changing transaction type is not allowed for updates']);
    }

    $catStmt = $pdo->prepare('SELECT id, name, category_type FROM categories WHERE id=:id');
    $catStmt->execute(['id' => $categoryId]);
    $category = $catStmt->fetch();
    if (!$category) {
        send_json(404, ['message' => 'Category not found']);
    }
    $subStmt = $pdo->prepare('SELECT id, name, category_id FROM sub_categories WHERE id=:id');
    $subStmt->execute(['id' => $subCategoryId]);
    $subCategory = $subStmt->fetch();
    if (!$subCategory) {
        send_json(404, ['message' => 'Sub-category not found']);
    }
    if ((int)$subCategory['category_id'] !== $categoryId) {
        send_json(400, ['message' => 'Sub-category does not belong to selected category']);
    }

    $stmt = $pdo->prepare('UPDATE expenses SET amount=:amount, description=:description, expense_date=:expenseDate, category_id=:categoryId, sub_category_id=:subCategoryId, updated_by=:updatedBy, updated_on=NOW(6) WHERE id=:id');
    $stmt->execute([
        'amount' => $amount,
        'description' => $description,
        'expenseDate' => $expenseDate,
        'categoryId' => $categoryId,
        'subCategoryId' => $subCategoryId,
        'updatedBy' => $currentUser ?? 'system',
        'id' => $id,
    ]);

    send_json(200, [
        'id' => $id,
        'amount' => $amount,
        'description' => $description,
        'expenseDate' => $expenseDate,
        'type' => $type,
        'categoryId' => $categoryId,
        'categoryName' => (string)$category['name'],
        'subCategoryId' => $subCategoryId,
        'subCategoryName' => (string)$subCategory['name'],
    ]);
}

if (
    $method === 'DELETE'
    && (
        preg_match('#^/api/transactions/(\d+)$#', $path, $m) === 1
        || preg_match('#^/api/expenses/(\d+)$#', $path, $m) === 1
    )
) {
    $id = (int)$m[1];
    $stmt = $pdo->prepare('DELETE FROM expenses WHERE id=:id');
    $stmt->execute(['id' => $id]);
    if ($stmt->rowCount() === 0) {
        send_json(404, ['message' => 'Transaction not found']);
    }
    send_json(204);
}

if ($method === 'GET' && $path === '/api/dashboard/summary') {
    $topN = (int)($_GET['topN'] ?? 5);
    if ($topN < 1 || $topN > 10) {
        send_json(400, ['message' => 'topN must be between 1 and 10']);
    }

    $today = new DateTimeImmutable('today');
    $currentMonth = new DateTimeImmutable($today->format('Y-m-01'));
    $startMonth = $currentMonth->modify('-11 months');
    $fromDate = $startMonth->format('Y-m-d');
    $toDate = $currentMonth->format('Y-m-t');

    $sql = 'SELECT e.amount, e.expense_date, e.transaction_type, c.name AS category_name, c.category_type
            FROM expenses e
            JOIN categories c ON c.id = e.category_id
            WHERE e.expense_date BETWEEN :fromDate AND :toDate
            ORDER BY e.expense_date DESC, e.id DESC';
    $stmt = $pdo->prepare($sql);
    $stmt->execute(['fromDate' => $fromDate, 'toDate' => $toDate]);
    $rows = $stmt->fetchAll();

    $months = [];
    for ($i = 0; $i < 12; $i++) {
        $m = $startMonth->modify("+$i months");
        $months[$m->format('Y-m')] = [];
    }
    foreach ($rows as $r) {
        $mk = substr((string)$r['expense_date'], 0, 7);
        if (isset($months[$mk])) {
            $months[$mk][] = $r;
        }
    }

    $sumByType = static function (array $items, string $type): float {
        $sum = 0.0;
        foreach ($items as $x) {
            if ((string)$x['transaction_type'] === $type) {
                $sum += (float)$x['amount'];
            }
        }
        return $sum;
    };
    $sumSaving = static function (array $items): float {
        $sum = 0.0;
        foreach ($items as $x) {
            if ((string)$x['category_type'] === 'SAVING') {
                $sum += (float)$x['amount'];
            }
        }
        return $sum;
    };
    $periodSummary = static function (array $items) use ($sumByType): array {
        $expense = $sumByType($items, 'EXPENSE');
        $income = $sumByType($items, 'INCOME');
        return ['expenseTotal' => (float)to_decimal($expense), 'incomeTotal' => (float)to_decimal($income), 'netAmount' => (float)to_decimal($income - $expense)];
    };

    $monthlyTotals = [];
    $monthlyIncomeExpensePoints = [];
    $monthlySavingRatePoints = [];
    foreach ($months as $mk => $items) {
        $expense = $sumByType($items, 'EXPENSE');
        $income = $sumByType($items, 'INCOME');
        $saving = $sumSaving($items);
        $savingPct = $income > 0 ? ($saving / $income) * 100 : 0.0;
        $monthlyTotals[] = ['yearMonth' => $mk, 'total' => (float)to_decimal($expense), 'count' => count($items)];
        $monthlyIncomeExpensePoints[] = [
            'yearMonth' => $mk,
            'incomeTotal' => (float)to_decimal($income),
            'expenseTotal' => (float)to_decimal($expense),
            'netAmount' => (float)to_decimal($income - $expense),
        ];
        $monthlySavingRatePoints[] = [
            'yearMonth' => $mk,
            'savingAmount' => (float)to_decimal($saving),
            'incomeTotal' => (float)to_decimal($income),
            'savingRatePercent' => (float)to_decimal($savingPct),
        ];
    }

    $currentMonthKey = $currentMonth->format('Y-m');
    $currentMonthItems = $months[$currentMonthKey] ?? [];
    $currentMonthSummary = $periodSummary($currentMonthItems);

    $samePeriodLastMonthStart = $currentMonth->modify('-1 month');
    $samePeriodLastMonthEnd = $samePeriodLastMonthStart->setDate(
        (int)$samePeriodLastMonthStart->format('Y'),
        (int)$samePeriodLastMonthStart->format('m'),
        min((int)$today->format('d'), (int)$samePeriodLastMonthStart->format('t'))
    );
    $samePeriodItems = array_values(array_filter($rows, static fn(array $r): bool => between_dates((string)$r['expense_date'], $samePeriodLastMonthStart->format('Y-m-d'), $samePeriodLastMonthEnd->format('Y-m-d'))));
    $samePeriodLastMonthSummary = $periodSummary($samePeriodItems);

    $last30Start = $today->modify('-29 days');
    $last30Items = array_values(array_filter($rows, static fn(array $r): bool => between_dates((string)$r['expense_date'], $last30Start->format('Y-m-d'), $today->format('Y-m-d'))));
    $last30DaysSummary = $periodSummary($last30Items);

    $lastMonthKey = $currentMonth->modify('-1 month')->format('Y-m');
    $lastMonthItems = $months[$lastMonthKey] ?? [];
    $lastMonthSummary = $periodSummary($lastMonthItems);

    $lastQuarterStart = $currentMonth->modify('-3 months')->format('Y-m-01');
    $lastQuarterEnd = $currentMonth->modify('-1 month')->format('Y-m-t');
    $lastQuarterItems = array_values(array_filter($rows, static fn(array $r): bool => between_dates((string)$r['expense_date'], $lastQuarterStart, $lastQuarterEnd)));
    $lastQuarterSummary = $periodSummary($lastQuarterItems);

    $lastYearStart = $today->modify('-364 days')->format('Y-m-d');
    $lastYearItems = array_values(array_filter($rows, static fn(array $r): bool => between_dates((string)$r['expense_date'], $lastYearStart, $today->format('Y-m-d'))));
    $lastYearSummary = $periodSummary($lastYearItems);

    $currentMonthCategoryBuckets = [];
    foreach ($currentMonthItems as $x) {
        if ((string)$x['transaction_type'] !== 'EXPENSE') {
            continue;
        }
        $key = (string)$x['category_name'];
        if (!isset($currentMonthCategoryBuckets[$key])) {
            $currentMonthCategoryBuckets[$key] = ['sum' => 0.0, 'count' => 0];
        }
        $currentMonthCategoryBuckets[$key]['sum'] += (float)$x['amount'];
        $currentMonthCategoryBuckets[$key]['count']++;
    }
    $currentMonthCategoryTotals = [];
    foreach ($currentMonthCategoryBuckets as $name => $bucket) {
        $currentMonthCategoryTotals[] = ['categoryName' => $name, 'total' => (float)to_decimal($bucket['sum']), 'count' => $bucket['count']];
    }
    usort($currentMonthCategoryTotals, static fn(array $a, array $b): int => $b['total'] <=> $a['total']);

    $yearlyBuckets = [];
    foreach ($rows as $x) {
        if ((string)$x['transaction_type'] !== 'EXPENSE') {
            continue;
        }
        $name = (string)$x['category_name'];
        if (!isset($yearlyBuckets[$name])) {
            $yearlyBuckets[$name] = [];
        }
        $yearlyBuckets[$name][] = $x;
    }
    $topYearlyCategoryTrends = [];
    foreach ($yearlyBuckets as $name => $items) {
        $total = 0.0;
        foreach ($items as $x) {
            $total += (float)$x['amount'];
        }
        $trend = [];
        foreach ($months as $mk => $monthItems) {
            $sum = 0.0;
            $count = 0;
            foreach ($monthItems as $x) {
                if ((string)$x['category_name'] === $name && (string)$x['transaction_type'] === 'EXPENSE') {
                    $sum += (float)$x['amount'];
                    $count++;
                }
            }
            $trend[] = ['yearMonth' => $mk, 'total' => (float)to_decimal($sum), 'count' => $count];
        }
        $topYearlyCategoryTrends[] = ['categoryName' => $name, 'yearTotal' => (float)to_decimal($total), 'monthlyTrend' => $trend];
    }
    usort($topYearlyCategoryTrends, static fn(array $a, array $b): int => $b['yearTotal'] <=> $a['yearTotal']);
    $topYearlyCategoryTrends = array_slice($topYearlyCategoryTrends, 0, $topN);

    send_json(200, [
        'currentMonthTotal' => $currentMonthSummary['expenseTotal'],
        'last30DaysTotal' => $last30DaysSummary['expenseTotal'],
        'lastMonthTotal' => $lastMonthSummary['expenseTotal'],
        'lastQuarterTotal' => $lastQuarterSummary['expenseTotal'],
        'lastYearTotal' => $lastYearSummary['expenseTotal'],
        'currentMonthSummary' => $currentMonthSummary,
        'samePeriodLastMonthSummary' => $samePeriodLastMonthSummary,
        'last30DaysSummary' => $last30DaysSummary,
        'lastMonthSummary' => $lastMonthSummary,
        'lastQuarterSummary' => $lastQuarterSummary,
        'lastYearSummary' => $lastYearSummary,
        'monthlyTotals' => $monthlyTotals,
        'monthlyIncomeExpensePoints' => $monthlyIncomeExpensePoints,
        'monthlySavingRatePoints' => $monthlySavingRatePoints,
        'currentMonthCategoryTotals' => $currentMonthCategoryTotals,
        'topYearlyCategoryTrends' => $topYearlyCategoryTrends,
    ]);
}

send_json(404, ['message' => 'Not found']);
