CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    budget_period VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_on TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_by VARCHAR(100),
    updated_on TIMESTAMP(6),
    CONSTRAINT uk_budgets_name UNIQUE (name)
);

SET @budget_default_exists = (
    SELECT COUNT(1)
    FROM budgets
    WHERE is_default = TRUE
);
INSERT INTO budgets (name, amount, budget_period, is_default, created_by, created_on)
SELECT 'Default Budget', 999999999.99, 'MONTHLY', TRUE, 'u001', CURRENT_TIMESTAMP(6)
WHERE @budget_default_exists = 0;

SET @budget_col_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'expenses'
      AND column_name = 'budget_id'
);
SET @budget_col_sql = IF(
    @budget_col_exists = 0,
    'ALTER TABLE expenses ADD COLUMN budget_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @budget_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @default_budget_id = (
    SELECT id
    FROM budgets
    WHERE is_default = TRUE
    ORDER BY id
    LIMIT 1
);

UPDATE expenses
SET budget_id = @default_budget_id
WHERE budget_id IS NULL;

SET @budget_fk_exists = (
    SELECT COUNT(1)
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'expenses'
      AND column_name = 'budget_id'
      AND referenced_table_name = 'budgets'
);
SET @budget_fk_sql = IF(
    @budget_fk_exists = 0,
    'ALTER TABLE expenses ADD CONSTRAINT fk_expenses_budget FOREIGN KEY (budget_id) REFERENCES budgets(id)',
    'SELECT 1'
);
PREPARE stmt FROM @budget_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @budget_col_nullable = (
    SELECT IS_NULLABLE
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'expenses'
      AND column_name = 'budget_id'
);
SET @budget_col_notnull_sql = IF(
    @budget_col_nullable = 'YES',
    'ALTER TABLE expenses MODIFY COLUMN budget_id BIGINT NOT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @budget_col_notnull_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
