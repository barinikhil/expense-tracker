SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'categories'
      AND column_name = 'category_type'
);
SET @col_sql = IF(
    @col_exists = 0,
    'ALTER TABLE categories ADD COLUMN category_type VARCHAR(20) NOT NULL DEFAULT ''EXPENSE''',
    'SELECT 1'
);
PREPARE stmt FROM @col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
