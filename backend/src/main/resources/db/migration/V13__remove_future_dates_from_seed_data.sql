UPDATE expenses
SET expense_date = CURRENT_DATE()
WHERE expense_date > CURRENT_DATE()
  AND (
      description LIKE 'Dummy expense - %'
      OR description LIKE 'Dummy income - %'
      OR description LIKE 'Dummy saving expense - %'
  );
