package com.example.findit.dao;

import com.example.findit.model.User;
import com.example.findit.util.DBConnection;

import java.sql.*;

public class UserDAO {



    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (id_number, full_name, password, role, contact_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getIdNumber());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getContactNumber());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean isIdNumberTaken(String idNumber) {
        String sql = "SELECT user_id FROM users WHERE id_number = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error checking ID number: " + e.getMessage());
            return false;
        }
    }

    public User loginUser(String idNumber, String password) {
        String sql = "SELECT * FROM users WHERE id_number = ? AND password_hash = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idNumber);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setIdNumber(rs.getString("id_number"));
                user.setFullName(rs.getString("full_name"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setContactNumber(rs.getString("contact_number"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }

        return null;
    }

    public User adminLogin(String password) {

        String sql =
                "SELECT * FROM users " +
                        "WHERE password_hash = ? " +
                        "AND role = 'Admin'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                System.out.println("ADMIN FOUND");

                User user = new User();

                user.setUserId(rs.getInt("user_id"));
                user.setIdNumber(rs.getString("id_number"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setContactNumber(rs.getString("contact_number"));

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}