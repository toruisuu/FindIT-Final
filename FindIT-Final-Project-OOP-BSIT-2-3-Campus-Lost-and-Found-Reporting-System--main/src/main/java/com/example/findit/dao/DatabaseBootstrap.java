package com.example.findit.dao;

import com.example.findit.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public final class DatabaseBootstrap {
    private static boolean initialized;

    private DatabaseBootstrap() {
    }

    public static synchronized void ensureApplicationSchema() {
        if (initialized) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                throw new IllegalStateException("Database connection is not available.");
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE items ADD COLUMN IF NOT EXISTS item_name VARCHAR(150)");
                stmt.executeUpdate("ALTER TABLE items ADD COLUMN IF NOT EXISTS image_path TEXT");
                stmt.executeUpdate("ALTER TABLE items ALTER COLUMN image_path TYPE TEXT");
                stmt.executeUpdate("ALTER TABLE claims ADD COLUMN IF NOT EXISTS claimant_name VARCHAR(100)");
                stmt.executeUpdate("ALTER TABLE claims ADD COLUMN IF NOT EXISTS student_number VARCHAR(30)");
                stmt.executeUpdate("ALTER TABLE claims ADD COLUMN IF NOT EXISTS contact_info VARCHAR(100)");
                stmt.executeUpdate("ALTER TABLE claims ADD COLUMN IF NOT EXISTS proof_description TEXT");
                stmt.executeUpdate("DROP TABLE IF EXISTS ingress_egress_logs");
                stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS activity_logs (
                            log_id SERIAL PRIMARY KEY,
                            user_id INTEGER,
                            action VARCHAR(50) NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT timezone('Asia/Manila', now())
                        )
                        """);
        stmt.executeUpdate("""
         CREATE TABLE IF NOT EXISTS validation_logs (
            validation_id   SERIAL      PRIMARY KEY,
            item_id         INT         NOT NULL,
            validated_by    INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
            validation_type VARCHAR(20) NOT NULL CHECK (validation_type IN ('APPROVED', 'REJECTED', 'PENDING')),
            remarks         VARCHAR(500),
            validated_at    TIMESTAMP   NOT NULL DEFAULT timezone('Asia/Manila', now())
        )
        """);
                stmt.executeUpdate("""
                        ALTER TABLE activity_logs
                        ALTER COLUMN created_at SET DEFAULT timezone('Asia/Manila', now())
                        """);
                stmt.executeUpdate("""
                        ALTER TABLE validation_logs
                        ALTER COLUMN validated_at SET DEFAULT timezone('Asia/Manila', now())
                        """);
            }

            for (String category : List.of("Electronics", "Wallet", "Documents", "Clothing", "Accessories", "Other")) {
                ensureCategory(conn, category);
            }

            initialized = true;
        } catch (Exception e) {
            throw new IllegalStateException("Could not prepare database schema.", e);
        }
    }

    static int ensureCategory(Connection conn, String categoryName) throws Exception {
        String selectSql = "SELECT category_id FROM categories WHERE LOWER(category_name) = LOWER(?) LIMIT 1";
        try (PreparedStatement select = conn.prepareStatement(selectSql)) {
            select.setString(1, categoryName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        }

        String insertSql = "INSERT INTO categories (category_name) VALUES (?) RETURNING category_id";
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            insert.setString(1, categoryName);
            ResultSet rs = insert.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        }

        throw new IllegalStateException("Could not create category: " + categoryName);
    }

    static int ensureUser(Connection conn, String idNumber, String fullName, String contactNumber) throws Exception {
        String selectSql = "SELECT user_id FROM users WHERE id_number = ? LIMIT 1";
        try (PreparedStatement select = conn.prepareStatement(selectSql)) {
            select.setString(1, idNumber);
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }

        String insertSql = """
                INSERT INTO users (id_number, full_name, password_hash, role, contact_number)
                VALUES (?, ?, ?, 'Student', ?)
                RETURNING user_id
                """;
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            insert.setString(1, idNumber);
            insert.setString(2, fullName);
            insert.setString(3, "findit-form-entry");
            insert.setString(4, contactNumber);
            ResultSet rs = insert.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }

        throw new IllegalStateException("Could not create user: " + fullName);
    }

    public static int ensureMonitoringUser(String idNumber, String fullName, String contactNumber) {
        ensureApplicationSchema();
        try (Connection conn = DBConnection.connect()) {
            return ensureUser(conn, idNumber, fullName, contactNumber);
        } catch (Exception e) {
            throw new IllegalStateException("Could not prepare monitoring user.", e);
        }
    }
}
