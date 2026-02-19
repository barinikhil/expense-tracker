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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class V4__seed_dummy_expenses extends BaseJavaMigration {

    private static final String DUMMY_PREFIX = "Dummy expense - ";
    private static final int MIN_RECORDS_PER_MONTH = 25;
    private static final int MAX_RECORDS_PER_MONTH = 50;
    private static final int MIN_CURRENT_MONTH_TOTAL_RECORDS = 15;
    private static final int MIN_CURRENT_MONTH_RECORDS_PER_CATEGORY = 15;
    private static final Map<String, AmountRange> AMOUNT_RANGES = Map.of(
            "Food", new AmountRange(8, 80),
            "Shopping", new AmountRange(20, 250),
            "Entertainment", new AmountRange(10, 150)
    );

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<CategoryBundle> bundles = loadCategoryBundles(connection);
        if (bundles.isEmpty()) {
            return;
        }

        Random random = new Random(20260218L);
        YearMonth currentMonth = YearMonth.now();

        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            YearMonth month = currentMonth.minusMonths(monthOffset);
            int targetCount = MIN_RECORDS_PER_MONTH + random.nextInt(MAX_RECORDS_PER_MONTH - MIN_RECORDS_PER_MONTH + 1);
            long existingDummyCount = countDummyForMonth(connection, month);
            long missingCount = Math.max(0, targetCount - existingDummyCount);

            for (int i = 0; i < missingCount; i++) {
                CategoryBundle bundle = bundles.get(random.nextInt(bundles.size()));
                Long subCategoryId = bundle.subCategoryIds().get(random.nextInt(bundle.subCategoryIds().size()));
                insertExpense(connection, month, LocalDate.now(), bundle, subCategoryId, random);
            }
        }

        long currentMonthTotal = countExpensesForMonth(connection, currentMonth);
        long missingTotal = Math.max(0, MIN_CURRENT_MONTH_TOTAL_RECORDS - currentMonthTotal);
        for (int i = 0; i < missingTotal; i++) {
            CategoryBundle bundle = bundles.get(random.nextInt(bundles.size()));
            Long subCategoryId = bundle.subCategoryIds().get(random.nextInt(bundle.subCategoryIds().size()));
            insertExpense(connection, currentMonth, LocalDate.now(), bundle, subCategoryId, random);
        }

        for (CategoryBundle bundle : bundles) {
            long categoryMonthCount = countExpensesForMonthByCategory(connection, currentMonth, bundle.categoryId());
            long missingForCategory = Math.max(0, MIN_CURRENT_MONTH_RECORDS_PER_CATEGORY - categoryMonthCount);
            for (int i = 0; i < missingForCategory; i++) {
                Long subCategoryId = bundle.subCategoryIds().get(random.nextInt(bundle.subCategoryIds().size()));
                insertExpense(connection, currentMonth, LocalDate.now(), bundle, subCategoryId, random);
            }
        }
    }

    private List<CategoryBundle> loadCategoryBundles(Connection connection) throws Exception {
        String categoriesSql = "SELECT id, name FROM categories";
        List<CategoryBundle> bundles = new ArrayList<>();
        try (PreparedStatement categoryStmt = connection.prepareStatement(categoriesSql);
             ResultSet categories = categoryStmt.executeQuery()) {
            while (categories.next()) {
                long categoryId = categories.getLong("id");
                String categoryName = categories.getString("name");
                List<Long> subCategoryIds = findSubCategoryIds(connection, categoryId);
                if (!subCategoryIds.isEmpty()) {
                    bundles.add(new CategoryBundle(categoryId, categoryName, subCategoryIds));
                }
            }
        }
        return bundles;
    }

    private List<Long> findSubCategoryIds(Connection connection, long categoryId) throws Exception {
        String sql = "SELECT id FROM sub_categories WHERE category_id = ?";
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id"));
                }
            }
        }
        return ids;
    }

    private long countDummyForMonth(Connection connection, YearMonth month) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM expenses
                WHERE description LIKE ?
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

    private long countExpensesForMonth(Connection connection, YearMonth month) throws Exception {
        String sql = "SELECT COUNT(*) FROM expenses WHERE expense_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, month.atDay(1));
            stmt.setObject(2, month.atEndOfMonth());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long countExpensesForMonthByCategory(Connection connection, YearMonth month, long categoryId) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM expenses
                WHERE expense_date BETWEEN ? AND ?
                  AND category_id = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, month.atDay(1));
            stmt.setObject(2, month.atEndOfMonth());
            stmt.setLong(3, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void insertExpense(
            Connection connection,
            YearMonth month,
            LocalDate today,
            CategoryBundle bundle,
            Long subCategoryId,
            Random random
    ) throws Exception {
        String sql = """
                INSERT INTO expenses (amount, description, expense_date, category_id, sub_category_id, created_by, created_on)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, randomAmountFor(bundle.categoryName(), random));
            stmt.setString(2, DUMMY_PREFIX + "Seeded");
            stmt.setObject(3, randomDateInMonth(month, today, random));
            stmt.setLong(4, bundle.categoryId());
            stmt.setLong(5, subCategoryId);
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

    private BigDecimal randomAmountFor(String categoryName, Random random) {
        AmountRange range = AMOUNT_RANGES.getOrDefault(categoryName, new AmountRange(10, 100));
        double value = range.min() + (range.max() - range.min()) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record CategoryBundle(long categoryId, String categoryName, List<Long> subCategoryIds) {}
    private record AmountRange(double min, double max) {}
}
