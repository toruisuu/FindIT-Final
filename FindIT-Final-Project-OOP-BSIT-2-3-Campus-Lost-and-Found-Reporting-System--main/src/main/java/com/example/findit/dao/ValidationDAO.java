package com.example.findit.dao;

import com.example.findit.model.ValidationLog;
import com.example.findit.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValidationDAO {
    public void logValidation(int itemId, int validatedBy,
                              String validationType, String remarks) {
        String sql = """
                INSERT INTO validation_logs
                    (item_id, validated_by, validation_type, remarks, validated_at)
                VALUES (?, ?, ?, ?, timezone('Asia/Manila', now()))
                """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ps.setInt(2, validatedBy);
            ps.setString(3, validationType);
            ps.setString(4, remarks);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ValidationLog> getAllValidations() {
        List<ValidationLog> list = new ArrayList<>();
        String sql = "SELECT * FROM validation_logs ORDER BY validated_at DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("validated_at");
                list.add(new ValidationLog(
                        rs.getInt("validation_id"),
                        rs.getInt("item_id"),
                        rs.getInt("validated_by"),
                        rs.getString("validation_type"),
                        rs.getString("remarks"),
                        ts != null ? ts.toLocalDateTime() : null
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countByType(String validationType) {
        String sql = "SELECT COUNT(*) FROM validation_logs WHERE validation_type = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, validationType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
