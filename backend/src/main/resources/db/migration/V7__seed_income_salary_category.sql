INSERT INTO categories (name, description, category_type, created_by, created_on)
SELECT 'Salary', 'Salary income', 'INCOME', 'u001', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Salary')
);

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'Salary', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('Salary')
  AND NOT EXISTS (
      SELECT 1 FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('Salary')
        AND sc.category_id = c.id
  );
