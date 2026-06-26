package com.example.findit.controllers.user;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ClaimRequest;
import com.example.findit.model.ItemReport;
import com.example.findit.util.InputValidator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ClaimItemsController {

    @FXML private TextField txtClaimantName;
    @FXML private TextField txtContact;
    @FXML private TextArea txtProofDescription;
    private ItemReport item;

    public void setItem(ItemReport item) {
        this.item = item;
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (item == null) {
            showAlert(Alert.AlertType.ERROR, "Item Missing", "Please open a claim from an item details window.");
            return;
        }

        if (!"Found".equalsIgnoreCase(item.getType())) {
            showAlert(Alert.AlertType.WARNING, "Claim Unavailable",
                    "Only found item reports can be claimed.");
            return;
        }

        if (txtClaimantName.getText().isBlank() || txtContact.getText().isBlank()
                || txtProofDescription.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all required fields.");
            return;
        }

        if (!InputValidator.isValidContact(txtContact.getText())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Contact",
                    "Please enter a valid 11-digit phone number or email address.");
            return;
        }

        if (!InputValidator.isValidNameText(txtClaimantName.getText())
                || !InputValidator.isValidDescriptionText(txtProofDescription.getText())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Characters",
                    "Please remove unsupported special characters from the claim.");
            return;
        }

        ClaimRequest savedClaim;
        try {
            savedClaim = AppDataStore.addClaimRequest(
                    item,
                    txtClaimantName.getText().trim(),
                    "",
                    txtContact.getText().trim(),
                    txtProofDescription.getText().trim()
            );
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "The claim could not be saved. Please try again.");
            return;
        }

        showClaimTrackingReceipt(savedClaim);
        closeWindow(event);
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showClaimTrackingReceipt(ClaimRequest savedClaim) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Submitted");
        alert.setHeaderText("Success! Your Claim is Saved.");

        TextField idField = new TextField(savedClaim.getTrackingId());
        idField.setEditable(false);
        idField.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-alignment: center; -fx-background-color: #F0F0F0;");

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-cursor: hand; -fx-background-color: #800000; -fx-text-fill: white;");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(savedClaim.getTrackingId());
            clipboard.setContent(content);
            copyBtn.setText("Copied!");
        });

        Button downloadBtn = new Button("Save as .txt File");
        downloadBtn.setStyle("-fx-cursor: hand; -fx-background-color: #E65100; -fx-text-fill: white;");
        downloadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Tracking ID");
            fileChooser.setInitialFileName("FindIT_Claim_Receipt_" + savedClaim.getTrackingId() + ".txt");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("--- FindIT Claim Tracking Receipt ---");
                    writer.println("Item: " + savedClaim.getItem().getItemName());
                    writer.println("Claimant: " + savedClaim.getClaimantName());
                    writer.println("Status: " + savedClaim.getStatus());
                    writer.println("Tracking ID: " + savedClaim.getTrackingId());
                    writer.println("--------------------------------------");
                    downloadBtn.setText("Saved!");
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Could not save the file.");
                }
            }
        });

        VBox layout = new VBox(15,
                new Label("Keep this ID to edit or delete your claim later:"),
                idField,
                new HBox(10, copyBtn, downloadBtn) {{ setAlignment(Pos.CENTER); }}
        );
        layout.setAlignment(Pos.CENTER);
        alert.getDialogPane().setContent(layout);
        alert.showAndWait();
    }
}
