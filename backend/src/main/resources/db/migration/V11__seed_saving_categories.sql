INSERT INTO categories (name, description, category_type, created_by, created_on)
SELECT 'Mutual Fund', 'Mutual fund investments', 'SAVING', 'u001', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('Mutual Fund')
);

INSERT INTO categories (name, description, category_type, created_by, created_on)
SELECT 'FD', 'Fixed deposit savings', 'SAVING', 'u001', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('FD')
);

INSERT INTO categories (name, description, category_type, created_by, created_on)
SELECT 'NPS', 'National Pension System savings', 'SAVING', 'u001', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE LOWER(name) = LOWER('NPS')
);

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'Debt', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('Mutual Fund')
  AND NOT EXISTS (
      SELECT 1
      FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('Debt')
        AND sc.category_id = c.id
  );

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'Equity', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('Mutual Fund')
  AND NOT EXISTS (
      SELECT 1
      FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('Equity')
        AND sc.category_id = c.id
  );

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'ELSS', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('Mutual Fund')
  AND NOT EXISTS (
      SELECT 1
      FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('ELSS')
        AND sc.category_id = c.id
  );

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'FD', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('FD')
  AND NOT EXISTS (
      SELECT 1
      FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('FD')
        AND sc.category_id = c.id
  );

INSERT INTO sub_categories (name, category_id, created_by, created_on)
SELECT 'NPS', c.id, 'u001', CURRENT_TIMESTAMP(6)
FROM categories c
WHERE LOWER(c.name) = LOWER('NPS')
  AND NOT EXISTS (
      SELECT 1
      FROM sub_categories sc
      WHERE LOWER(sc.name) = LOWER('NPS')
        AND sc.category_id = c.id
  );
