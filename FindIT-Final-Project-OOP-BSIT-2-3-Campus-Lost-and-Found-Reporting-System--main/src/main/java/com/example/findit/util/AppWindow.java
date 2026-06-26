package com.example.findit.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class AppWindow {
    private static final double DEFAULT_WIDTH = 1100.0;
    private static final double DEFAULT_HEIGHT = 720.0;
    private static final double MIN_WIDTH = 900.0;
    private static final double MIN_HEIGHT = 580.0;

    private AppWindow() {
    }

    public static void show(Stage stage, Parent root, String title) {
        configure(stage);
        stage.setTitle(title);
        stage.setScene(new Scene(root, preferredWidth(), preferredHeight()));
        stage.show();
        maximizeWhenUseful(stage);
    }

    public static void setRoot(Stage stage, Parent root, String title) {
        boolean wasMaximized = stage.isMaximized();
        configure(stage);
        stage.setTitle(title);

        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(root, preferredWidth(), preferredHeight()));
        } else {
            scene.setRoot(root);
        }

        stage.setMaximized(wasMaximized);
    }

    public static void configure(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setResizable(true);
        stage.setMinWidth(Math.min(MIN_WIDTH, bounds.getWidth()));
        stage.setMinHeight(Math.min(MIN_HEIGHT, bounds.getHeight()));
    }

    private static double preferredWidth() {
        return Math.min(DEFAULT_WIDTH, Screen.getPrimary().getVisualBounds().getWidth());
    }

    private static double preferredHeight() {
        return Math.min(DEFAULT_HEIGHT, Screen.getPrimary().getVisualBounds().getHeight());
    }

    private static void maximizeWhenUseful(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        if (bounds.getWidth() >= DEFAULT_WIDTH && bounds.getHeight() >= DEFAULT_HEIGHT) {
            stage.setMaximized(true);
        }
    }
}
