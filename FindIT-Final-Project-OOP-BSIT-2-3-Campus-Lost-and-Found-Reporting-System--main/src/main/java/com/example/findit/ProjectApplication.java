package com.example.findit;

import com.example.findit.model.AppDataStore;
import com.example.findit.util.AppWindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class ProjectApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProjectApplication.class.getResource("views/user/MainPortal.fxml"));
        Parent root = fxmlLoader.load();
        AppWindow.show(stage, root, "Campus Lost and Found System");
    }

    @Override
    public void stop() {
        AppDataStore.stopRealtimeUpdates();
    }
}
