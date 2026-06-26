package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemMatch;
import com.example.findit.model.ItemReport;
import com.example.findit.util.ImageStorage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MatchSuggestionController implements Initializable {
    @FXML private Label statusBadge;
    @FXML private Label lostItemName;
    @FXML private Label lostCategory;
    @FXML private Label lostReportedBy;
    @FXML private Label lostContact;
    @FXML private Label lostDate;
    @FXML private Label lostLocation;
    @FXML private Label lostDescription;
    @FXML private VBox lostImageBox;
    @FXML private Label foundItemName;
    @FXML private Label foundCategory;
    @FXML private Label foundReportedBy;
    @FXML private Label foundContact;
    @FXML private Label foundDate;
    @FXML private Label foundLocation;
    @FXML private Label foundDescription;
    @FXML private VBox foundImageBox;
    @FXML private Button confirmButton;
    @FXML private Button declineButton;

    private ItemMatch currentMatch;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void loadMatch(ItemMatch match) {
        this.currentMatch = match;
        setStatus(match.getStatus());
        populateLostItem(match.getLostItem());
        populateFoundItem(match.getFoundItem());
    }

    private void populateLostItem(ItemReport item) {
        lostItemName.setText(item.getItemName());
        lostCategory.setText("Category: " + safe(item.getCategory()));
        lostReportedBy.setText("Reported by: " + safe(item.getReportedBy()));
        lostContact.setText("Contact: " + safe(item.getContact()));
        lostDate.setText("Date: " + safe(item.getDate()));
        lostLocation.setText("Location: " + safe(item.getLocation()));
        lostDescription.setText("Description: " + safe(item.getDescription()));
        renderImage(lostImageBox, item);
    }

    private void populateFoundItem(ItemReport item) {
        foundItemName.setText(item.getItemName());
        foundCategory.setText("Category: " + safe(item.getCategory()));
        foundReportedBy.setText("Reported by: " + safe(item.getReportedBy()));
        foundContact.setText("Contact: " + safe(item.getContact()));
        foundDate.setText("Date: " + safe(item.getDate()));
        foundLocation.setText("Location: " + safe(item.getLocation()));
        foundDescription.setText("Description: " + safe(item.getDescription()));
        renderImage(foundImageBox, item);
    }

    private void renderImage(VBox imageBox, ItemReport item) {
        if (imageBox == null) {
            return;
        }

        imageBox.getChildren().clear();
        imageBox.setAlignment(Pos.CENTER);

        Image image = ImageStorage.loadImage(item.getImagePath());
        if (image == null) {
            Label placeholder = new Label("No Image Available");
            placeholder.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");
            imageBox.getChildren().add(placeholder);
            return;
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(340);
        imageView.setFitHeight(185);
        imageView.setPreserveRatio(true);
        imageBox.getChildren().add(imageView);
    }

    @FXML
    private void handleConfirmMatch() {
        if (currentMatch == null) {
            return;
        }

        try {
            AppDataStore.confirmMatch(currentMatch);
            setStatus("Confirmed");
            showAlert(Alert.AlertType.INFORMATION, "Match Confirmed",
                    "An approved claim has been added to the Claims tab.");
            closeDialog();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "The match could not be confirmed. Please try again.");
        }
    }

    @FXML
    private void handleDeclineMatch() {
        if (currentMatch == null) {
            return;
        }

        AppDataStore.rejectMatch(currentMatch);
        showAlert(Alert.AlertType.INFORMATION, "Match Declined",
                "This match suggestion has been recorded as a rejected claim.");
        closeDialog();
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    private void setStatus(String status) {
        statusBadge.setText(status);
        if ("Confirmed".equalsIgnoreCase(status)) {
            statusBadge.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 12; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            if (confirmButton != null) {
                confirmButton.setDisable(true);
                confirmButton.setText("Match Confirmed");
            }
            if (declineButton != null) {
                declineButton.setDisable(true);
            }
        } else {
            statusBadge.setStyle("-fx-background-color: #FFE0B2; -fx-background-radius: 12; -fx-text-fill: #E65100; -fx-font-weight: bold;");
            if (confirmButton != null) {
                confirmButton.setDisable(false);
                confirmButton.setText("Confirm Match");
            }
            if (declineButton != null) {
                declineButton.setDisable(false);
            }
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) statusBadge.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
