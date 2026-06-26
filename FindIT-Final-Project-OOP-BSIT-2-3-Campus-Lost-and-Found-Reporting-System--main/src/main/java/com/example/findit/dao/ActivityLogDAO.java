package com.example.findit.dao;

import com.example.findit.model.ActivityLog;
import com.example.findit.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    public void log(int userId, String action, String description) {
        String sql = """
        INSERT INTO activity_logs
        (user_id, action, description, created_at)
        VALUES (?, ?, ?, timezone('Asia/Manila', now()))
        """;

        try {
            DatabaseBootstrap.ensureApplicationSchema();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recordIngressEgress(int userId, String eventType, String description) {
        log(userId, eventType, description);
    }
    public List<ActivityLog> getIngressEgressLogs() {
        List<ActivityLog> list = new ArrayList<>();
        String sql = """
                SELECT * FROM activity_logs
                WHERE action IN ('CHECK_IN', 'CHECK_OUT')
                ORDER BY created_at DESC
                """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getInt("user_id"));
                log.setAction(rs.getString("action"));
                log.setDescription(rs.getString("description"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                log.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTodayByAction(String action) {
        String sql = """
                SELECT COUNT(*) FROM activity_logs
                WHERE action = ?
                  AND created_at >= (timezone('Asia/Manila', now()))::date
                  AND created_at < ((timezone('Asia/Manila', now()))::date + INTERVAL '1 day')
                """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, action);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
