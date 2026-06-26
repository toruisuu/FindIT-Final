package com.example.findit.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public final class InputValidator {
    private static final String NAME_PATTERN = "[A-Za-z0-9 .,'-]+";
    private static final String DESCRIPTION_PATTERN = "[A-Za-z0-9 .,'()/-]+";
    private static final String PHONE_PATTERN = "\\d{11}";
    private static final String EMAIL_PATTERN = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
    private static final String STUDENT_NUMBER_PATTERN = "\\d{4}-\\d{5}-[A-Za-z]{2}-\\d";

    private InputValidator() {
    }

    public static LocalDate today() {
        String sql = "SELECT (timezone('Asia/Manila', now()))::date";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDate(1).toLocalDate();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not get the current Philippine date from the database.", e);
        }
        throw new IllegalStateException("Could not get the current Philippine date from the database.");
    }

    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(today());
    }

    public static boolean isValidContact(String value) {
        String trimmed = trim(value);
        return trimmed.matches(PHONE_PATTERN) || trimmed.matches(EMAIL_PATTERN);
    }

    public static boolean isValidStudentNumber(String value) {
        return trim(value).matches(STUDENT_NUMBER_PATTERN);
    }

    public static String formatStudentNumber(String value) {
        String compact = trim(value).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (!compact.matches("\\d{9}[A-Z]{2}\\d")) {
            return trim(value).toUpperCase();
        }

        return compact.substring(0, 4)
                + "-"
                + compact.substring(4, 9)
                + "-"
                + compact.substring(9, 11)
                + "-"
                + compact.substring(11);
    }

    public static boolean isValidNameText(String value) {
        return trim(value).matches(NAME_PATTERN);
    }

    public static boolean isValidDescriptionText(String value) {
        return trim(value).matches(DESCRIPTION_PATTERN);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
