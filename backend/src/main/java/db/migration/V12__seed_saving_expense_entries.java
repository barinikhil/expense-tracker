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
import java.util.Random;

public class V12__seed_saving_expense_entries extends BaseJavaMigration {

    private static final String DUMMY_PREFIX = "Dummy saving expense - ";
    private static final int MONTHS_TO_SEED = 18;
    private static final int TARGET_RECORDS_PER_MONTH = 3;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<CategoryBundle> bundles = loadSavingCategoryBundles(connection);
        if (bundles.isEmpty()) {
            return;
        }

        Random random = new Random(20260219L);
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        for (int monthOffset = MONTHS_TO_SEED - 1; monthOffset >= 0; monthOffset--) {
            YearMonth month = currentMonth.minusMonths(monthOffset);
            long existing = countDummySavingExpenseForMonth(connection, month);
            long missing = Math.max(0, TARGET_RECORDS_PER_MONTH - existing);
            for (int i = 0; i < missing; i++) {
                CategoryBundle bundle = bundles.get(random.nextInt(bundles.size()));
                long subCategoryId = bundle.subCategoryIds().get(random.nextInt(bundle.subCategoryIds().size()));
                insertSavingExpense(connection, month, today, bundle.categoryId(), subCategoryId, random);
            }
        }
    }

    private List<CategoryBundle> loadSavingCategoryBundles(Connection connection) throws Exception {
        String categoriesSql = "SELECT id FROM categories WHERE category_type = 'SAVING' AND created_by = 'u001' ORDER BY id";
        List<CategoryBundle> bundles = new ArrayList<>();
        try (PreparedStatement categoryStmt = connection.prepareStatement(categoriesSql);
             ResultSet categories = categoryStmt.executeQuery()) {
            while (categories.next()) {
                long categoryId = categories.getLong("id");
                List<Long> subCategoryIds = findSubCategoryIds(connection, categoryId);
                if (!subCategoryIds.isEmpty()) {
                    bundles.add(new CategoryBundle(categoryId, subCategoryIds));
                }
            }
        }
        return bundles;
    }

    private List<Long> findSubCategoryIds(Connection connection, long categoryId) throws Exception {
        String sql = "SELECT id FROM sub_categories WHERE category_id = ? ORDER BY id";
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

    private long countDummySavingExpenseForMonth(Connection connection, YearMonth month) throws Exception {
        String sql = """
                SELECT COUNT(*)
                FROM expenses e
                JOIN categories c ON c.id = e.category_id
                WHERE e.created_by = 'u001'
                  AND c.category_type = 'SAVING'
                  AND e.transaction_type = 'EXPENSE'
                  AND e.description LIKE ?
                  AND e.expense_date BETWEEN ? AND ?
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

    private void insertSavingExpense(
            Connection connection,
            YearMonth month,
            LocalDate today,
            long categoryId,
            long subCategoryId,
            Random random
    ) throws Exception {
        String sql = """
                INSERT INTO expenses (
                    amount, description, expense_date, category_id, sub_category_id, transaction_type, created_by, created_on
                )
                VALUES (?, ?, ?, ?, ?, 'EXPENSE', ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, randomSavingExpenseAmount(random));
            stmt.setString(2, DUMMY_PREFIX + "Seeded");
            stmt.setObject(3, randomDateInMonth(month, today, random));
            stmt.setLong(4, categoryId);
            stmt.setLong(5, subCategoryId);
            stmt.setString(6, "u001");
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

    private BigDecimal randomSavingExpenseAmount(Random random) {
        double value = 250 + (5000 - 250) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record CategoryBundle(long categoryId, List<Long> subCategoryIds) {
    }
}
