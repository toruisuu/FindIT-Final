package com.example.findit.controllers.user;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemReport;
import com.example.findit.util.ImageStorage;
import com.example.findit.util.toast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ItemDetailsController {
    @FXML private VBox imageBox;
    @FXML private Label lblItemName, lblCategory, lblStatus, lblDate, lblLocation, lblReportedBy, lblDescription;
    
    @FXML private Button btnClaimItem;
    @FXML private Button btnEditItem;
    @FXML private Button btnDeleteItem;

    private ItemReport item;
    private boolean managerMode;

    public void setItem(ItemReport item) {
        this.item = item;
        refreshLabels();

        if ("Lost".equalsIgnoreCase(item.getType())) {
            lblStatus.setStyle("-fx-background-color: #FFCDD2; -fx-background-radius: 10; -fx-text-fill: #C62828;");
        } else {
            lblStatus.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 10; -fx-text-fill: #2E7D32;");
        }

        boolean canClaim = "Found".equalsIgnoreCase(item.getType());
        if (btnClaimItem != null) {
            btnClaimItem.setVisible(canClaim);
            btnClaimItem.setManaged(canClaim);
        }

        // SECURITY LOGIC: Show image if Manager OR if it's a Lost item
        boolean isLost = "Lost".equalsIgnoreCase(item.getType());
        boolean showDetails = managerMode || isLost;

        imageBox.getChildren().clear();
        if (!showDetails) {
            imageBox.getChildren().add(createProtectedPlaceholder());
        } else if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            ImageView imageView = createImageView(item.getImagePath());
            if (imageView != null) {
                imageBox.getChildren().add(imageView);
            } else {
                imageBox.getChildren().add(createPlaceholder(item));
            }
        } else {
            imageBox.getChildren().add(createPlaceholder(item));
        }
    }

    private void refreshLabels() {
        lblItemName.setText(safe(item.getItemName()));
        lblStatus.setText(safe(item.getType()));

        // SECURITY LOGIC: Show labels if Manager OR if it's a Lost item
        boolean isLost = "Lost".equalsIgnoreCase(item.getType());
        boolean showDetails = managerMode || isLost;

        if (showDetails) {
            lblCategory.setText(safe(item.getCategory()));
            lblDate.setText(safe(item.getDate()));
            lblLocation.setText(safe(item.getLocation()));
            lblReportedBy.setText(safe(item.getReportedBy()));
            lblDescription.setText(safe(item.getDescription()));
        } else {
            lblCategory.setText("Hidden");
            lblDate.setText("Hidden");
            lblLocation.setText("Hidden");
            lblReportedBy.setText("Hidden");
            lblDescription.setText("Details are hidden from public users to prevent false claims. You must describe this item accurately in your claim request.");
        }
    }

    // --- NEW: THE MANAGER MODE ---
    public void enableManagerMode() {
        managerMode = true;
        if (item != null) {
            setItem(item);
        }
        // Hide the claim button
        if (btnClaimItem != null) {
            btnClaimItem.setVisible(false);
            btnClaimItem.setManaged(false);
        }
        // Reveal Edit and Delete
        if (btnEditItem != null && btnDeleteItem != null) {
            btnEditItem.setVisible(true);
            btnEditItem.setManaged(true);
            btnDeleteItem.setVisible(true);
            btnDeleteItem.setManaged(true);
        }
    }

    @FXML
    public void handleEditItem(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Item Details");
        dialog.setHeaderText("Update information for: " + item.getItemName());

        TextField nameField = new TextField(item.getItemName());
        TextField locField = new TextField(item.getLocation());
        TextArea descArea = new TextArea(item.getDescription());
        descArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Item Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);  grid.add(locField, 1, 1);
        grid.add(new Label("Description:"), 0, 2); grid.add(descArea, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Update the Database
            AppDataStore.updateItemDetails(item, nameField.getText(), locField.getText(), descArea.getText());
            
            // Update the local object and refresh the UI labels
            item.setItemName(nameField.getText());
            item.setLocation(locField.getText());
            item.setDescription(descArea.getText());
            refreshLabels();
            
            toast.show(((Node) event.getSource()).getScene().getWindow(), "Item updated successfully!", "success");
        }
    }

    @FXML
    public void handleDeleteItem(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Archive Item");
        confirm.setHeaderText("Archive this Record");
        confirm.setContentText("Are you sure you want to archive this item? It will be removed from the public board but kept in the database history.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            
            AppDataStore.archiveItemReport(item); 
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            
            // NOTE: The main table will still auto-refresh because we remove it from the ObservableList in AppDataStore!
        }
    }

    private ImageView createImageView(String imagePath) {
        Image image = ImageStorage.loadImage(imagePath);
        if (image == null) return null;
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(190);
        imageView.setFitWidth(210);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private Label createPlaceholder(ItemReport item) {
        Label placeholder = new Label(safe(item.getType()) + " Item");
        placeholder.setStyle("-fx-text-fill: #999999; -fx-font-weight: bold;");
        return placeholder;
    }

    private Label createProtectedPlaceholder() {
        Label placeholder = new Label("Image hidden");
        placeholder.setStyle("-fx-text-fill: #999999; -fx-font-weight: bold;");
        return placeholder;
    }

    @FXML
    public void handleClaimItem(ActionEvent event) {
        if (item == null || !"Found".equalsIgnoreCase(item.getType())) {
            showAlert(Alert.AlertType.WARNING, "Claim Unavailable", "Only found item reports can be claimed.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/findit/views/user/ClaimItems.fxml"));
            Parent root = loader.load();
            ClaimItemsController controller = loader.getController();
            controller.setItem(item);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Claim Item");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Claim Form Error", "The claim form could not be opened.");
        }
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private String safe(String value) { return value == null ? "" : value; }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
