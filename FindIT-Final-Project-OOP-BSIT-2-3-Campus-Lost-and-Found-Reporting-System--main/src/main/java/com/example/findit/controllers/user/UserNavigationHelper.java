package com.example.findit.controllers.user;

import com.example.findit.util.AppWindow;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

final class UserNavigationHelper {

    private UserNavigationHelper() { }

    static void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(UserNavigationHelper.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            AppWindow.setRoot(stage, newRoot, titleFor(fxmlPath));
        } catch (IOException e) {
            System.err.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    static void switchScene(Node source, String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(UserNavigationHelper.class.getResource(fxmlPath));
            Stage stage = (Stage) source.getScene().getWindow();
            AppWindow.setRoot(stage, newRoot, titleFor(fxmlPath));
        } catch (IOException e) {
            System.err.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private static String titleFor(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            return "Campus Lost and Found System";
        }

        int slash = fxmlPath.lastIndexOf('/');
        int dot = fxmlPath.lastIndexOf('.');
        String name = fxmlPath.substring(slash + 1, dot > slash ? dot : fxmlPath.length());
        return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }
}
