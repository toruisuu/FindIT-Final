package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ClaimRequest;
import com.example.findit.model.ItemMatch;
import com.example.findit.model.ItemReport;
import com.example.findit.util.ImageStorage;
import com.example.findit.util.toast;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClaimsController implements Initializable {

    @FXML private AdminSidebarController sidebarController;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> viewToggle;
    @FXML private Button archivedItemsButton;

    @FXML private TableView<ClaimRow> claimsTable;
    
    // Updated to exactly match the clean 5-column layout in FXML
    @FXML private TableColumn<ClaimRow, String> colItemName, colDate, colClaimant, colStatus, colAction;

    private final ObservableList<ClaimRow> masterData = FXCollections.observableArrayList();
    private FilteredList<ClaimRow> filteredData;
    private boolean showingArchived;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) { sidebarController.setActiveTab("Claims"); }
        
        statusFilter.setItems(FXCollections.observableArrayList("All Status", "Pending", "Approved", "Rejected"));
        statusFilter.getSelectionModel().selectFirst();
        updateArchiveButton();
        
        if (viewToggle != null) {
            viewToggle.setItems(FXCollections.observableArrayList("Active Claims", "Archived History"));
            viewToggle.getSelectionModel().selectFirst();
            
            viewToggle.valueProperty().addListener((obs, oldVal, newVal) -> {
                showingArchived = "Archived History".equals(newVal);
                refreshTableData();
                updateArchiveButton();
                claimsTable.refresh();
            });
        }
        
        configureTableColumns();
        claimsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        refreshTableData();
        wireSearchAndFilter();
        
        AppDataStore.getClaimRequests().addListener((javafx.collections.ListChangeListener<ClaimRequest>) change -> {
            if (!showingArchived) {
                refreshTableData();
            }
        });
    }

    @FXML
    private void handleToggleArchivedItems() {
        showingArchived = !showingArchived;
        if (viewToggle != null) {
            viewToggle.setValue(showingArchived ? "Archived History" : "Active Claims");
        }
        if (showingArchived) {
            AppDataStore.refreshArchivedClaims();
        }
        refreshTableData();
        updateArchiveButton();
        claimsTable.refresh();
    }

    private void updateArchiveButton() {
        if (archivedItemsButton != null) {
            archivedItemsButton.setText(showingArchived ? "Active Claims" : "Archived Claims");
            archivedItemsButton.setStyle(showingArchived
                    ? "-fx-background-color: #FFCC00; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: #4A1212; -fx-font-weight: bold;"
                    : "-fx-background-color: #800000; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        }
        if (claimsTable != null) {
            claimsTable.setStyle(showingArchived
                    ? "-fx-control-inner-background: #f4f4f4;"
                    : "-fx-control-inner-background: #ffffff;");
        }
    }

    private void configureTableColumns() {
        // Matches the properties in ClaimRow class
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colClaimant.setCellValueFactory(new PropertyValueFactory<>("claimantName")); 
        colStatus.setCellValueFactory(new PropertyValueFactory<>("claimStatus"));

        colStatus.setCellFactory(col -> new TableCell<ClaimRow, String>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    if (item.equalsIgnoreCase("Pending")) {
                        badge.setStyle("-fx-background-color: #FFE0B2; -fx-text-fill: #E65100; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    } else if (item.equalsIgnoreCase("Approved")) {
                        badge.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    } else if (item.equalsIgnoreCase("Rejected")) {
                        badge.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    }
                    setGraphic(badge);
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<ClaimRow, String>() {
            private final Button approveBtn = new Button();
            private final Button rejectBtn  = new Button();
            private final Button archiveBtn = new Button("Archive");
            private final Button restoreBtn = new Button("Restore");
            private final Button viewBtn    = new Button();

            {
                approveBtn.setGraphic(createIcon("/com/example/findit/assets/check.png"));
                rejectBtn.setGraphic(createIcon("/com/example/findit/assets/ekis.png"));
                viewBtn.setGraphic(createIcon("/com/example/findit/assets/ViewEye.png"));

                String transparentStyle = "-fx-background-color: transparent; -fx-cursor: hand;";
                approveBtn.setStyle(transparentStyle);
                rejectBtn.setStyle(transparentStyle);
                viewBtn.setStyle(transparentStyle);
                archiveBtn.setStyle("-fx-background-color: #800000; -fx-background-radius: 7; -fx-cursor: hand; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-padding: 5 9 5 9;");
                restoreBtn.setStyle("-fx-background-color: #FFCC00; -fx-background-radius: 7; -fx-cursor: hand; -fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-padding: 5 9 5 9;");

                approveBtn.setOnAction(e -> handleApprove(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(e  -> handleReject(getTableView().getItems().get(getIndex())));
                archiveBtn.setOnAction(e -> handleArchiveClaim(getTableView().getItems().get(getIndex())));
                viewBtn.setOnAction(e -> showClaimDetails(getTableView().getItems().get(getIndex()), "Claim Details"));
                restoreBtn.setOnAction(e -> handleRestoreClaim(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ClaimRow row = getTableRow().getItem();
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(2);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    boolean isArchived = showingArchived;

                    if (isArchived) {
                        box.getChildren().addAll(viewBtn, restoreBtn);
                    } else {
                        if ("Pending".equalsIgnoreCase(row.getClaimStatus())) {
                            box.getChildren().addAll(approveBtn, rejectBtn, archiveBtn);
                        } else {
                            box.getChildren().addAll(viewBtn, archiveBtn);
                        }
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void refreshTableData() {
        boolean isArchived = showingArchived;
        ObservableList<ClaimRequest> sourceList = isArchived ? AppDataStore.ARCHIVED_CLAIMS : AppDataStore.getClaimRequests();
        
        masterData.setAll(sourceList.stream()
                .map(ClaimRow::new)
                .toList());
                
        if (filteredData != null) {
            applyFilter();
        }
    }

    private void handleArchiveClaim(ClaimRow item) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Archive Claim Confirmation");
        confirmDialog.setHeaderText("Archive Claim: " + item.getItemName());
        confirmDialog.setContentText("Are you sure you want to archive this claim request? It will be moved to the history logs.");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AppDataStore.archiveClaimRequest(item.getRequest());
                refreshTableData();
            }
        });
    }

    private void handleRestoreClaim(ClaimRow item) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Restore Claim Confirmation");
        confirmDialog.setHeaderText("Restore Claim: " + item.getItemName());
        confirmDialog.setContentText("Bring this claim request back to the active claims list?");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AppDataStore.restoreClaimRequest(item.getRequest());
                AppDataStore.refreshArchivedClaims();
                refreshTableData();
            }
        });
    }

    @FXML
    private void handleShowMatchSuggestions() {
        AppDataStore.refreshAll();
        showMatchSuggestionsDialog();
    }

    private void showMatchSuggestionsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Match Suggestions");
        dialog.setHeaderText("Potential lost and found matches");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(780);

        VBox list = new VBox(14);
        list.setPadding(new Insets(10));

        // Filter out matches for items that already have an approved claim
        var matches = AppDataStore.getMatchSuggestions().stream()
                .filter(m -> !hasApprovedClaim(m.getFoundItem().getId()))
                .toList();

        if (matches.isEmpty()) {
            Label empty = new Label("No match suggestions available.");
            empty.setStyle("-fx-text-fill: #777777; -fx-font-size: 15;");
            list.getChildren().add(empty);
        } else {
            for (ItemMatch match : matches) {
                list.getChildren().add(createMatchSuggestionCard(match, list, dialog));
            }
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(460);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
        refreshTableData();
    }

    /** Returns true if the given item ID already has an Approved claim. */
    private boolean hasApprovedClaim(int itemId) {
        return AppDataStore.getClaimRequests().stream()
                .anyMatch(c -> c.getItem().getId() == itemId
                        && "Approved".equalsIgnoreCase(c.getStatus()));
    }

    private VBox createMatchSuggestionCard(ItemMatch match, VBox cardContainer, Dialog<ButtonType> parentDialog) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E6E6E6; -fx-border-radius: 10;");

        Label title = new Label("Potential Match");
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 16;");

        Label status = new Label(match.getStatus());
        status.setStyle("-fx-background-color: #FFE0B2; -fx-background-radius: 12; -fx-text-fill: #E65100; -fx-font-weight: bold; -fx-padding: 3 10 3 10;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, status);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox comparison = new HBox(14,
                createMatchItemSummary(match.getLostItem()),
                createMatchItemSummary(match.getFoundItem())
        );

        Button detailsButton = new Button("View Match Details");
        detailsButton.setMaxWidth(Double.MAX_VALUE);
        detailsButton.setPrefHeight(38);
        detailsButton.setStyle("-fx-background-color: #800000; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        detailsButton.setOnAction(event -> {
            // Close the list dialog before opening the detail modal
            parentDialog.close();
            openMatchDetail(match);
            // Re-open the suggestions list so the user can continue reviewing
            // (claimed items will be gone from the refreshed list)
            AppDataStore.refreshAll();
            showMatchSuggestionsDialog();
        });

        card.getChildren().addAll(header, comparison, detailsButton);
        return card;
    }

    private VBox createMatchItemSummary(ItemReport item) {
        VBox box = new VBox(5);
        box.setPrefWidth(320);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("Lost".equalsIgnoreCase(item.getType())
                ? "-fx-border-color: #FFCDD2; -fx-border-radius: 8; -fx-border-width: 1.5;"
                : "-fx-border-color: #A5D6A7; -fx-border-radius: 8; -fx-border-width: 1.5;");
        HBox.setHgrow(box, Priority.ALWAYS);

        Label type = new Label(item.getType().toUpperCase() + " ITEM");
        type.setStyle("Lost".equalsIgnoreCase(item.getType())
                ? "-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 10;"
                : "-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 10;");
        Label name = new Label(item.getItemName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #222222; -fx-font-weight: bold;");
        Label reporter = new Label("Reported by: " + safe(item.getReportedBy()));
        reporter.setWrapText(true);
        reporter.setStyle("-fx-text-fill: #777777; -fx-font-size: 12;");

        box.getChildren().addAll(type, name, reporter);
        return box;
    }

    private void openMatchDetail(ItemMatch match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/findit/views/admin/MatchSuggestion.fxml"));
            Parent root = loader.load();

            MatchSuggestionController controller = loader.getController();
            controller.loadMatch(match);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (claimsTable.getScene() != null) {
                dialog.initOwner(claimsTable.getScene().getWindow());
            }
            dialog.setTitle("Match Details");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Match Suggestions");
            alert.setHeaderText(null);
            alert.setContentText("Could not open the match details.");
            alert.showAndWait();
        }
    }

    private void wireSearchAndFilter() {
        filteredData = new FilteredList<>(masterData, p -> true);
        claimsTable.setItems(filteredData);

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statusValue = statusFilter.getValue() == null ? "All Status" : statusFilter.getValue();

        filteredData.setPredicate(row -> {
            boolean matchesSearch = search.isEmpty()
                    || row.getItemName().toLowerCase().contains(search)
                    || row.getClaimantName().toLowerCase().contains(search)
                    || row.getClaimTrackingId().toLowerCase().contains(search)
                    || row.getItemTrackingId().toLowerCase().contains(search)
                    || row.getCategory().toLowerCase().contains(search)
                    || row.getLocation().toLowerCase().contains(search);

            boolean matchesStatus = "All Status".equals(statusValue)
                    || row.getClaimStatus().equalsIgnoreCase(statusValue);

            return matchesSearch && matchesStatus;
        });
    }

    private void handleApprove(ClaimRow row) {
        if (!confirmStatusChange("Approve Claim", "Are you sure about approving this claim?")) {
            return;
        }
        AppDataStore.updateClaimStatus(row.getRequest(), "Approved");
        refreshTableData();
        toast.show(claimsTable.getScene().getWindow(), "Claim successfully Approved!", "success");
    }

    private void handleReject(ClaimRow row) {
        if (!confirmStatusChange("Reject Claim", "Are you sure about rejecting this claim?")) {
            return;
        }
        AppDataStore.updateClaimStatus(row.getRequest(), "Rejected");
        refreshTableData();
        toast.show(claimsTable.getScene().getWindow(), "Claim has been Rejected.", "error");
    }

    private boolean confirmStatusChange(String title, String message) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(title);
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText(message);
        return confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showClaimDetails(ClaimRow row, String title) {
        ClaimRequest claim = row.getRequest();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(row.getItemName() + " - " + row.getClaimStatus());
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
        ButtonType closeBtn = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        String currentStatus = row.getClaimStatus();
        
        ButtonType revertBtn = new ButtonType("Undo / Revert to Pending", ButtonBar.ButtonData.LEFT);
        ButtonType approveBtn = new ButtonType("Change to Approved", ButtonBar.ButtonData.OTHER);
        ButtonType rejectBtn = new ButtonType("Change to Rejected", ButtonBar.ButtonData.OTHER);

        boolean isArchived = showingArchived;
        
        if (!isArchived) {
            if (currentStatus.equalsIgnoreCase("Approved")) {
                dialog.getDialogPane().getButtonTypes().addAll(revertBtn, rejectBtn);
            } else if (currentStatus.equalsIgnoreCase("Rejected")) {
                dialog.getDialogPane().getButtonTypes().addAll(revertBtn, approveBtn);
            }
        }
    
        dialog.showAndWait().ifPresent(response -> {
            javafx.stage.Window window = claimsTable.getScene().getWindow();

            if (response == revertBtn) {
                AppDataStore.updateClaimStatus(claim, "Pending");
                toast.show(window, "Claim reverted to Pending.", "warning");
            } else if (response == approveBtn) {
                if (!confirmStatusChange("Approve Claim", "Are you sure about approving this claim?")) {
                    return;
                }
                AppDataStore.updateClaimStatus(claim, "Approved");
                toast.show(window, "Claim successfully Approved!", "success");
            } else if (response == rejectBtn) {
                if (!confirmStatusChange("Reject Claim", "Are you sure about rejecting this claim?")) {
                    return;
                }
                AppDataStore.updateClaimStatus(claim, "Rejected");
                toast.show(window, "Claim has been Rejected.", "error");
            }
            
            refreshTableData(); 
        });
    }

    private VBox createImagePanel(ItemReport item) {
        VBox panel = new VBox(8);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(260);

        StackPane imageFrame = new StackPane();
        imageFrame.setPrefSize(260, 240);
        imageFrame.setStyle("-fx-background-color: #F0F0F3; -fx-background-radius: 10;");

        Image image = ImageStorage.loadImage(item.getImagePath());
        if (image == null) {
            Label placeholder = new Label("No Image Available");
            placeholder.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");
            imageFrame.getChildren().add(placeholder);
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(240);
            imageView.setFitHeight(220);
            imageView.setPreserveRatio(true);
            imageFrame.getChildren().add(imageView);
        }

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
        addDetailRow(grid, 0, "Claim Tracking ID", claim.getTrackingId());
        addDetailRow(grid, 1, "Claimant", claim.getClaimantName());
        addDetailRow(grid, 2, "Student Number", claim.getStudentNumber());
        addDetailRow(grid, 3, "Contact", claim.getContactInfo());
        addDetailRow(grid, 4, "Status", claim.getStatus());
        return grid;
    }

    private GridPane createItemGrid(ItemReport item) {
        GridPane grid = createDetailsGrid();
        addDetailRow(grid, 0, "Item Tracking ID", item.getTrackingId());
        addDetailRow(grid, 1, "Category", item.getCategory());
        addDetailRow(grid, 2, "Date", item.getDate());
        addDetailRow(grid, 3, "Location", item.getLocation());
        addDetailRow(grid, 4, "Reported By", item.getReportedBy());
        addDetailRow(grid, 5, "Reporter Contact", item.getContact());
        addDetailRow(grid, 6, "Description", item.getDescription());
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

    private ImageView createIcon(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("❌ Missing table icon: " + path);
            return new ImageView(); 
        }
        ImageView imgView = new ImageView(new Image(stream));
        imgView.setFitWidth(20);  
        imgView.setFitHeight(20);
        imgView.setPreserveRatio(true);
        return imgView;
    }
    
    public static class ClaimRow {
        private final ClaimRequest request;

        public ClaimRow(ClaimRequest request) {
            this.request = request;
        }

        public ClaimRequest getRequest() { return request; }
        public String getType() { return request.getItem().getType(); }
        public String getItemName() { return request.getItem().getItemName(); }
        public String getCategory() { return request.getItem().getCategory(); }
        public String getDate() { return request.getItem().getDate(); }
        public String getLocation() { return request.getItem().getLocation(); }
        public String getClaimantName() { return request.getClaimantName(); }
        public String getStudentNumber() { return request.getStudentNumber(); }
        public String getContactInfo() { return request.getContactInfo(); }
        public String getProofDescription() { return request.getProofDescription(); }
        public String getClaimStatus() { return request.getStatus(); }
        public String getClaimTrackingId() { return display(request.getTrackingId()); }
        public String getItemTrackingId() { return display(request.getItem().getTrackingId()); }
        public void setClaimStatus(String s) { request.setStatus(s); }

        private static String display(String value) {
            return value == null || value.isBlank() ? "N/A" : value;
        }
    }
}
