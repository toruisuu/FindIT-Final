package com.example.findit.controllers;

import com.example.findit.util.AppWindow;
import com.example.findit.model.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.application.Platform;

public class MainPortalController {

    // Memory flag to ensure it only shows once per application launch
    private static boolean hasSeenPrivacyPolicy = false;

    @FXML
    public void initialize() {
        if (!hasSeenPrivacyPolicy) {
            Platform.runLater(() -> {
                showPrivacyPolicyDialog();
                hasSeenPrivacyPolicy = true; 
            });
        }
    }

    @FXML
    public void goToUserDashboard(ActionEvent event) {
        SessionManager.checkInGuestUser();
        switchScene(event, "/com/example/findit/views/user/Dashboard.fxml");
    }

    @FXML
    public void goToAdminLogin(ActionEvent event) {
        switchScene(event, "/com/example/findit/views/admin/AdminLogin.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            String title = fxmlPath.contains("AdminLogin") ? "Admin Login" : "Campus Lost and Found System";
            AppWindow.setRoot(stage, root, title);
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not load the FXML file at " + fxmlPath);
            e.printStackTrace(); 
        }
    }

    private void showPrivacyPolicyDialog() {
        try {
            com.example.findit.controllers.user.PrivacyPolicyDialog.show(null);
        } catch (Exception e) {
            System.err.println("Could not load Privacy Policy: " + e.getMessage());
        }
    }
}