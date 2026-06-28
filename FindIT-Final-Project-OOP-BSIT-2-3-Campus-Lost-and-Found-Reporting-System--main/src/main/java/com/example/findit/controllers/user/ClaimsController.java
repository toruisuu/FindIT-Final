package com.example.findit.controllers.user;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ClaimRequest;
import com.example.findit.model.ItemReport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public class ClaimsController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane claimsFlow;

    @FXML
    public void initialize() {
        UserSidebarController.setActivePage("Claims");
        claimsFlow.setPrefWrapLength(700);
        claimsFlow.widthProperty().addListener((obs, oldWidth, newWidth) ->
                claimsFlow.setPrefWrapLength(newWidth.doubleValue()));
        statusFilter.setItems(FXCollections.observableArrayList("All Status", "Pending", "Approved", "Rejected"));
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> renderClaims());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderClaims());
        AppDataStore.getClaimRequests().addListener((javafx.collections.ListChangeListener<ClaimRequest>) change -> renderClaims());
        renderClaims();
    }

    private void renderClaims() {
        claimsFlow.getChildren().clear();
        List<ClaimRequest> filteredClaims = AppDataStore.getClaimRequests().stream()
                .filter(this::matchesFilters)
                .toList();

        if (filteredClaims.isEmpty()) {
            Label emptyLabel = new Label("No claims found.");
            emptyLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 16;");
            claimsFlow.getChildren().add(emptyLabel);
            return;
        }

        for (ClaimRequest claim : filteredClaims) {
            claimsFlow.getChildren().add(createClaimCard(claim));
        }
    }

    private boolean matchesFilters(ClaimRequest claim) {
        String search = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter.getValue();

        boolean matchesSearch = search.isEmpty()
                || safeContains(claim.getItem().getItemName(), search)
                || safeContains(claim.getClaimantName(), search)
                || safeContains(claim.getItem().getCategory(), search)
                || safeContains(claim.getItem().getLocation(), search);
        boolean matchesStatus = "All Status".equals(status)
                || claim.getStatus().equalsIgnoreCase(status);

        return matchesSearch && matchesStatus;
    }

    private boolean safeContains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private VBox createClaimCard(ClaimRequest claim) {
        VBox card = new VBox(7);
        card.setPrefWidth(230);
        card.setMinWidth(210);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 4); -fx-cursor: hand;");
        card.setOnMouseClicked(event -> showClaimDetails(claim));

        Label itemName = new Label(claim.getItem().getItemName());
        itemName.setWrapText(true);
        itemName.setStyle("-fx-text-fill: #4A1515; -fx-font-weight: bold; -fx-font-size: 15;");

        Label claimant = new Label("Claimant: " + claim.getClaimantName());
        claimant.setWrapText(true);
        claimant.setStyle("-fx-text-fill: #555555;");

        Label location = new Label("Item details hidden");
        location.setWrapText(true);
        location.setStyle("-fx-text-fill: #777777;");

        Label badge = new Label(claim.getStatus());
        badge.setStyle(statusStyle(claim.getStatus()));

        card.getChildren().addAll(itemName, claimant, location, badge);
        return card;
    }

    private void showClaimDetails(ClaimRequest claim) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Claim Details");
        dialog.setHeaderText(claim.getItem().getItemName() + " - " + claim.getStatus());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(820);

        HBox content = new HBox(22);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(10, 5, 5, 5));

        VBox details = new VBox(14);
        details.setPrefWidth(490);
        details.getChildren().addAll(
                createSection("Claim Information", createClaimGrid(claim)),
                createSection("Item Information", createItemGrid(claim.getItem())),
                createProofBlock(claim)
        );
        HBox.setHgrow(details, Priority.ALWAYS);

        content.getChildren().addAll(createImagePanel(claim.getItem()), details);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private VBox createImagePanel(ItemReport item) {
        VBox panel = new VBox(8);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(260);

        StackPane imageFrame = new StackPane();
        imageFrame.setPrefSize(260, 240);
        imageFrame.setStyle("-fx-background-color: #F0F0F3; -fx-background-radius: 10;");

        Label placeholder = new Label("Image hidden");
        placeholder.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");
        imageFrame.getChildren().add(placeholder);

        Label imageLabel = new Label(item.getItemName());
        imageLabel.setWrapText(true);
        imageLabel.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold;");
        panel.getChildren().addAll(imageFrame, imageLabel);
        return panel;
    }

    private VBox createSection(String titleText, GridPane grid) {
        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 13;");
        return new VBox(7, title, grid);
    }

    private GridPane createClaimGrid(ClaimRequest claim) {
        GridPane grid = createDetailsGrid();
        addDetailRow(grid, 0, "Claimant", claim.getClaimantName());
        addDetailRow(grid, 1, "Student Number", claim.getStudentNumber());
        addDetailRow(grid, 2, "Contact", claim.getContactInfo());
        addDetailRow(grid, 3, "Status", claim.getStatus());
        return grid;
    }

    private GridPane createItemGrid(ItemReport item) {
        GridPane grid = createDetailsGrid();
        addDetailRow(grid, 0, "Item Name", item.getItemName());
        addDetailRow(grid, 1, "Status", item.getType());
        addDetailRow(grid, 2, "Details", "Hidden from users to prevent false claims.");
        return grid;
    }

    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(125);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelColumn, valueColumn);
        return grid;
    }

    private VBox createProofBlock(ClaimRequest claim) {
        Label title = new Label("Proof");
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 13;");

        Label proof = new Label(safe(claim.getProofDescription()));
        proof.setWrapText(true);
        proof.setMinHeight(80);
        proof.setStyle("-fx-background-color: #F7F7F9; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #333333;");
        return new VBox(7, title, proof);
    }

    private void addDetailRow(GridPane grid, int row, String labelText, String valueText) {
        Label label = new Label(labelText + ":");
        label.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");

        Label value = new Label(safe(valueText));
        value.setWrapText(true);
        value.setStyle("-fx-text-fill: #222222;");
        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String statusStyle(String status) {
        if ("Approved".equalsIgnoreCase(status)) {
            return "-fx-background-color: #C8E6C9; -fx-background-radius: 12; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 3 10 3 10;";
        }
        if ("Rejected".equalsIgnoreCase(status)) {
            return "-fx-background-color: #FFCDD2; -fx-background-radius: 12; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-padding: 3 10 3 10;";
        }
        return "-fx-background-color: #FFE0B2; -fx-background-radius: 12; -fx-text-fill: #E65100; -fx-font-weight: bold; -fx-padding: 3 10 3 10;";
    }

    @FXML
    public void handleManageClaim(javafx.event.ActionEvent event) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Manage Claim Submission");
        dialog.setHeaderText("Secure Access");
        dialog.setContentText("Enter your Claim Tracking ID (e.g., CD-5678):");

        dialog.showAndWait().ifPresent(trackingId -> {
            String sanitizedId = trackingId.trim().toUpperCase();

            if (!sanitizedId.matches("[A-Z]{2}-\\d{4}")) {
                com.example.findit.util.toast.show(((javafx.scene.Node) event.getSource()).getScene().getWindow(), "Invalid Format. Must be LL-NNNN.", "error");
                return;
            }

            // Search for the claim matching the tracking ID
            com.example.findit.model.ClaimRequest foundClaim = com.example.findit.model.AppDataStore.getClaimRequests().stream()
                    .filter(claim -> sanitizedId.equals(claim.getTrackingId()))
                    .findFirst()
                    .orElse(null);

            if (foundClaim != null) {
                showClaimManagerWindow(foundClaim, event);
            } else {
                com.example.findit.util.toast.show(((javafx.scene.Node) event.getSource()).getScene().getWindow(), "No claim found with ID: " + sanitizedId, "warning");
            }
        });
    }

    private void showClaimManagerWindow(com.example.findit.model.ClaimRequest claim, javafx.event.ActionEvent event) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Edit Claim");
        dialog.setHeaderText("Managing Claim for: " + claim.getItem().getItemName());

        // Create Editable Fields
        javafx.scene.control.TextField contactField = new javafx.scene.control.TextField(claim.getContactInfo());
        javafx.scene.control.TextArea proofArea = new javafx.scene.control.TextArea(claim.getProofDescription());
        proofArea.setPrefRowCount(3);

        // Layout the form
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new javafx.scene.control.Label("Contact Info:"), 0, 0); grid.add(contactField, 1, 0);
        grid.add(new javafx.scene.control.Label("Proof Details:"), 0, 1); grid.add(proofArea, 1, 1);
        
        VBox content = new VBox(16);
        content.getChildren().addAll(createClaimTimeline(claim), grid);
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().setContent(content);

        // Add Action Buttons
        javafx.scene.control.ButtonType saveBtn = new javafx.scene.control.ButtonType("Save Changes", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType deleteBtn = new javafx.scene.control.ButtonType("Delete Claim", javafx.scene.control.ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, deleteBtn, javafx.scene.control.ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            javafx.stage.Window window = ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            if (response == saveBtn) {
                com.example.findit.model.AppDataStore.updateClaimDetails(
                        claim, 
                        contactField.getText(), 
                        proofArea.getText()
                );
                com.example.findit.util.toast.show(window, "Claim updated successfully!", "success");
            } else if (response == deleteBtn) {
                com.example.findit.model.AppDataStore.deleteClaimRequest(claim);
                com.example.findit.util.toast.show(window, "Claim withdrawn permanently.", "warning");
            }
        });
    }

    private VBox createClaimTimeline(ClaimRequest claim) {
        String status = safe(claim.getStatus());
        boolean approved = "Approved".equalsIgnoreCase(status);
        boolean rejected = "Rejected".equalsIgnoreCase(status);
        boolean pending  = !approved && !rejected;

        Label title = new Label("Claim Timeline");
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 13;");

        // Step 3 appearance depends on outcome
        String step3Label  = rejected ? "Rejected Claim" : "Approved Claim";
        boolean step3Done  = approved || rejected;

        HBox timeline = new HBox(10);
        timeline.setAlignment(Pos.CENTER_LEFT);
        timeline.getChildren().addAll(
                createTimelineStep("1", "Submitted Claim", true,  false,   false),
                createTimelineConnector(true),
                createTimelineStep("2", "Pending Review",  true,  pending, false),
                createTimelineConnector(step3Done),
                createTimelineStep("3", step3Label,        step3Done, step3Done, rejected)
        );

        VBox wrapper = new VBox(8, title, timeline);
        wrapper.setPadding(new Insets(12));
        wrapper.setStyle("-fx-background-color: #F7F7F9; -fx-background-radius: 10;");
        return wrapper;
    }

    /**
     * @param number   step number text
     * @param text     label below the circle
     * @param complete whether the step has been reached
     * @param current  whether this is the active step (bold label)
     * @param isRejected whether this step represents a rejection (red styling)
     */
    private VBox createTimelineStep(String number, String text,
                                    boolean complete, boolean current, boolean isRejected) {
        Label marker = new Label(complete ? number : "");
        marker.setAlignment(Pos.CENTER);
        marker.setMinSize(28, 28);
        marker.setPrefSize(28, 28);
        marker.setMaxSize(28, 28);

        String markerColor = !complete  ? "#D8D8D8"
                           : isRejected ? "#C62828"
                           :              "#800000";
        marker.setStyle(complete
                ? "-fx-background-color: " + markerColor + "; -fx-background-radius: 14; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;"
                : "-fx-background-color: " + markerColor + "; -fx-background-radius: 14;");

        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(130);
        if (current && isRejected) {
            label.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
        } else if (current) {
            label.setStyle("-fx-text-fill: #800000; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-text-fill: #555555;");
        }

        VBox step = new VBox(6, marker, label);
        step.setAlignment(Pos.TOP_CENTER);
        step.setPrefWidth(135);
        return step;
    }

    private Region createTimelineConnector(boolean complete) {
        Region connector = new Region();
        connector.setPrefSize(48, 3);
        connector.setMaxHeight(3);
        connector.setStyle(complete
                ? "-fx-background-color: #800000; -fx-background-radius: 2;"
                : "-fx-background-color: #D8D8D8; -fx-background-radius: 2;");
        return connector;
    }
}
