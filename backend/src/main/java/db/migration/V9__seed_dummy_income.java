    package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;

public class V9__seed_dummy_income extends BaseJavaMigration {

    private static final String DUMMY_PREFIX = "Dummy income - ";
    private static final int MONTHS_TO_SEED = 18;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        CategoryBundle incomeCategory = findIncomeCategoryWithSubCategory(connection);
        if (incomeCategory == null) {
            return;
        }

        Random random = new Random(20260218L);
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        YearMonth startMonth = currentMonth.minusMonths(MONTHS_TO_SEED - 1);
        long preCount = countDummyIncomeInRange(connection, startMonth, currentMonth);

        for (int monthOffset = MONTHS_TO_SEED - 1; monthOffset >= 0; monthOffset--) {
            YearMonth month = currentMonth.minusMonths(monthOffset);
            if (countDummyIncomeForMonth(connection, month) > 0) {
                continue;
            }
            insertIncome(connection, month, today, incomeCategory, random);
        }

        long postCount = countDummyIncomeInRange(connection, startMonth, currentMonth);
        System.out.println("V9__seed_dummy_income pre-count: " + preCount + ", post-count: " + postCount);
    }

    private CategoryBundle findIncomeCategoryWithSubCategory(Connection connection) throws Exception {
        String sql = """
                SELECT c.id AS category_id, sc.id AS sub_category_id
                FROM categories c
                JOIN sub_categories sc ON sc.category_id = c.id
                WHERE c.category_type = 'INCOME'
                ORDER BY
                    CASE WHEN LOWER(c.name) = 'salary' THEN 0 ELSE 1 END,
                    c.id,
                    sc.id
                LIMIT 1
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new CategoryBundle(rs.getLong("category_id"), rs.getLong("sub_category_id"));
        }
    }

    private long countDummyIncomeForMonth(Connection connection, YearMonth month) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM expenses
                WHERE transaction_type = 'INCOME'
                  AND description LIKE ?
                  AND expense_date BETWEEN ? AND ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, DUMMY_PREFIX + "%");
            stmt.setObject(2, month.atDay(1));
            stmt.setObject(3, month.atEndOfMonth());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long countDummyIncomeInRange(Connection connection, YearMonth startMonth, YearMonth endMonth) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM expenses
                WHERE transaction_type = 'INCOME'
                  AND description LIKE ?
                  AND expense_date BETWEEN ? AND ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, DUMMY_PREFIX + "%");
            stmt.setObject(2, startMonth.atDay(1));
            stmt.setObject(3, endMonth.atEndOfMonth());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void insertIncome(
            Connection connection,
            YearMonth month,
            LocalDate today,
            CategoryBundle categoryBundle,
            Random random
    ) throws Exception {
        String sql = """
                INSERT INTO expenses (
                    amount, description, expense_date, category_id, sub_category_id, transaction_type, created_by, created_on
                )
                VALUES (?, ?, ?, ?, ?, 'INCOME', ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, randomIncomeAmount(random));
            stmt.setString(2, DUMMY_PREFIX + "Salary Seeded");
            stmt.setObject(3, randomDateInMonth(month, today, random));
            stmt.setLong(4, categoryBundle.categoryId());
            stmt.setLong(5, categoryBundle.subCategoryId());
            stmt.setString(6, "system");
            stmt.setObject(7, LocalDateTime.now());
            stmt.executeUpdate();
        }
    }

    private LocalDate randomDateInMonth(YearMonth month, LocalDate today, Random random) {
        boolean isCurrentMonth = today.getYear() == month.getYear() && today.getMonth() == month.getMonth();
        int maxDay = isCurrentMonth ? today.getDayOfMonth() : month.lengthOfMonth();
        int day = 1 + random.nextInt(Math.max(1, maxDay));
        return month.atDay(day);
    }

    private BigDecimal randomIncomeAmount(Random random) {
        double value = 2000 + (9000 - 2000) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record CategoryBundle(long categoryId, long subCategoryId) {
    }
}
