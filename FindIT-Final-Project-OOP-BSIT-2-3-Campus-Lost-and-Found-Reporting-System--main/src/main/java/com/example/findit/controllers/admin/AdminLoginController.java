package com.example.findit.controllers.admin;

import com.example.findit.dao.UserDAO;
import com.example.findit.model.SessionManager;
import com.example.findit.model.User;
import com.example.findit.util.AppWindow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class AdminLoginController {

    @FXML
    private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {

        String password = passwordField.getText();

        if (password == null || password.trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Password cannot be empty."
            );

            return;
        }

        User admin = userDAO.adminLogin(password);

        if (admin != null) {

            System.out.println("Admin Logged In: "
                    + admin.getFullName());
            SessionManager.checkIn(admin, "Admin Portal");

            navigateTo(
                    "/com/example/findit/views/admin/AdminDashboard.fxml",
                    "Admin Dashboard"
            );

        } else {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Invalid Admin Password."
            );

            passwordField.clear();
        }
    }

    @FXML
    private void goBackToMain() {

        navigateTo(
                "/com/example/findit/views/user/MainPortal.fxml",
                "FindIT"
        );
    }

    private void navigateTo(String fxmlPath,
                            String title) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(fxmlPath)
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) passwordField
                            .getScene()
                            .getWindow();

            AppWindow.setRoot(stage, root, title);

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Navigation Error",
                    "Unable to load page."
            );
        }
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
