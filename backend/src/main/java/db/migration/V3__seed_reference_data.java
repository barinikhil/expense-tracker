package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class V3__seed_reference_data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        seedUsers(connection);
        seedCategoriesAndSubCategories(connection);
    }

    private void seedUsers(Connection connection) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        seedUserIfMissing(connection, "u001", encoder.encode("pass111"));
        seedUserIfMissing(connection, "u002", encoder.encode("pass111"));
    }

    private void seedUserIfMissing(Connection connection, String username, String passwordHash) throws Exception {
        String existsSql = "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement exists = connection.prepareStatement(existsSql)) {
            exists.setString(1, username);
            try (ResultSet rs = exists.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String insertSql = """
                INSERT INTO users (username, password_hash, active, created_by, created_on)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            insert.setString(1, username);
            insert.setString(2, passwordHash);
            insert.setBoolean(3, true);
            insert.setString(4, "system");
            insert.setObject(5, LocalDateTime.now());
            insert.executeUpdate();
        }
    }

    private void seedCategoriesAndSubCategories(Connection connection) throws Exception {
        Map<String, List<String>> defaultData = new LinkedHashMap<>();
        defaultData.put("Housing", List.of("Rent", "Maintenance", "Electricity", "Water Bill", "Internet"));
        defaultData.put("Food", List.of("Groceries", "Restaurants", "Snacks", "Coffee"));
        defaultData.put("Transportation", List.of("Fuel", "Public Transport", "Cab / Taxi", "Vehicle Maintenance"));
        defaultData.put("Health", List.of("Doctor Consultation", "Medicines", "Health Insurance", "Medical Tests"));
        defaultData.put("Entertainment", List.of("Movies", "OTT Subscription", "Games", "Travel"));
        defaultData.put("Education", List.of("Course Fees", "Books", "Certifications"));
        defaultData.put("Shopping", List.of("Clothing", "Electronics", "Household Items"));

        for (Map.Entry<String, List<String>> entry : defaultData.entrySet()) {
            long categoryId = findOrCreateCategory(connection, entry.getKey());
            for (String subCategoryName : entry.getValue()) {
                findOrCreateSubCategory(connection, subCategoryName, categoryId);
            }
        }
    }

    private long findOrCreateCategory(Connection connection, String name) throws Exception {
        String findSql = "SELECT id FROM categories WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement find = connection.prepareStatement(findSql)) {
            find.setString(1, name);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        String insertSql = """
                INSERT INTO categories (name, description, is_saving, created_by, created_on)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, name);
            insert.setString(2, name + " expenses");
            insert.setBoolean(3, false);
            insert.setString(4, "system");
            insert.setObject(5, LocalDateTime.now());
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new IllegalStateException("Unable to create category: " + name);
    }

    private void findOrCreateSubCategory(Connection connection, String name, long categoryId) throws Exception {
        String findSql = """
                SELECT 1
                FROM sub_categories
                WHERE LOWER(name) = LOWER(?)
                  AND category_id = ?
                """;
        try (PreparedStatement find = connection.prepareStatement(findSql)) {
            find.setString(1, name);
            find.setLong(2, categoryId);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String insertSql = """
                INSERT INTO sub_categories (name, category_id, created_by, created_on)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            insert.setString(1, name);
            insert.setLong(2, categoryId);
            insert.setString(3, "system");
            insert.setObject(4, LocalDateTime.now());
            insert.executeUpdate();
        }
    }
}
