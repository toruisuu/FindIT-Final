package com.example.findit.model;

import com.example.findit.dao.ActivityLogDAO;
import com.example.findit.dao.DatabaseBootstrap;

public final class SessionManager {
    private static final ActivityLogDAO ACTIVITY_LOG_DAO = new ActivityLogDAO();
    private static User currentUser;
    private static boolean checkedIn;

    private SessionManager() {
    }

    public static void checkIn(User user, String source) {
        if (user == null) {
            return;
        }

        if (checkedIn && currentUser != null && currentUser.getUserId() == user.getUserId()) {
            return;
        }

        currentUser = user;
        checkedIn = true;
        try {
            ACTIVITY_LOG_DAO.recordIngressEgress(
                    user.getUserId(),
                    "CHECK_IN",
                    user.getFullName() + " checked in through " + source + "."
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void checkInGuestUser() {
        int userId;
        try {
            userId = DatabaseBootstrap.ensureMonitoringUser(
                    "GUEST-USER",
                    "Guest User",
                    "N/A"
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }

        User guest = new User();
        guest.setUserId(userId);
        guest.setIdNumber("GUEST-USER");
        guest.setFullName("Guest User");
        guest.setRole("Student");
        guest.setContactNumber("N/A");

        checkIn(guest, "User Portal");
    }

    public static void checkOut(String destination) {
        if (!checkedIn || currentUser == null) {
            return;
        }

        try {
            ACTIVITY_LOG_DAO.recordIngressEgress(
                    currentUser.getUserId(),
                    "CHECK_OUT",
                    currentUser.getFullName() + " checked out to " + destination + "."
            );
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        checkedIn = false;
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isCurrentUserAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }
}
