package com.example.findit.util;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class toast {

    public static void show(Window window, String message, String type) {
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        String color;
        if (type.equalsIgnoreCase("success")) {
            color = "#2E7D32"; // Green for Approved
        } else if (type.equalsIgnoreCase("error")) {
            color = "#C62828"; // Red for Rejected
        } else if (type.equalsIgnoreCase("warning")) {
            color = "#E65100"; // Deep Orange for Pending/Revert
        } else {
            color = "#333333"; // Dark Gray fallback
        }

        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                       "-fx-padding: 10 20 10 20; -fx-background-radius: 20; " +
                       "-fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        popup.getContent().add(label);
        popup.setOnShown(e -> {
            popup.setX(window.getX() + (window.getWidth() / 2) - (label.getWidth() / 2));
            popup.setY(window.getY() + window.getHeight() - 100);
        });

        popup.show(window);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(1.5));
        fadeOut.setOnFinished(e -> popup.hide());
        fadeOut.play();
    }
}