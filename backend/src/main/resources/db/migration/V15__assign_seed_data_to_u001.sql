UPDATE categories
SET created_by = 'u001'
WHERE created_by = 'system';

UPDATE sub_categories
SET created_by = 'u001'
WHERE created_by = 'system';

UPDATE expenses
SET created_by = 'u001'
WHERE created_by = 'system'
  AND (
      description LIKE 'Dummy expense - %'
      OR description LIKE 'Dummy income - %'
      OR description LIKE 'Dummy saving expense - %'
  );

UPDATE budgets
SET created_by = 'u001'
WHERE created_by = 'system';

