SET @col_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'categories'
      AND column_name = 'is_saving'
);
SET @col_sql = IF(
    @col_exists = 1,
    'ALTER TABLE categories DROP COLUMN is_saving',
    'SELECT 1'
);
PREPARE stmt FROM @col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
