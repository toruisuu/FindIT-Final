package com.example.findit.controllers.user;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemReport;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ItemsController {
    private static final double CARD_MIN_WIDTH = 185.0;
    private static final double GRID_GAP = 20.0;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private GridPane itemsGrid;

    private static String initialSearch = "";
    private static String initialCategory = "All Categories";
    private static String initialStatus = "All Status";

    private int currentColumnCount = 4;

    public static void openWithFilters(String search, String category, String status) {
        initialSearch = search == null ? "" : search;
        initialCategory = category == null || category.isBlank() ? "All Categories" : category;
        initialStatus = status == null || status.isBlank() ? "All Status" : status;
    }

    private boolean isExpired(ItemReport item) {
        try {
            LocalDate itemDate = LocalDate.parse(item.getDate());
            LocalDate today = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(itemDate, today);
            return daysBetween > 60; 
        } catch (Exception e) {
            return false;
        }
    }
    
    @FXML
    public void initialize() {
        UserSidebarController.setActivePage("Items");
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All Categories", "Electronics", "Wallet", "Documents", "Clothing", "Accessories", "Other"
        ));
        statusFilter.setItems(FXCollections.observableArrayList("All Status", "Lost", "Found"));
        categoryFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> renderItems());
        categoryFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderItems());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderItems());
        applyInitialFilters();
        
        // This existing listener is what powers your auto-refresh!
        AppDataStore.getItemReports().addListener((javafx.collections.ListChangeListener<ItemReport>) change -> renderItems());
        itemsGrid.widthProperty().addListener((obs, oldWidth, newWidth) -> updateColumnsAndRender(newWidth.doubleValue()));
        renderItems();
    }

    private void applyInitialFilters() {
        searchField.setText(initialSearch);
        selectFilterValue(categoryFilter, initialCategory);
        selectFilterValue(statusFilter, initialStatus);

        initialSearch = "";
        initialCategory = "All Categories";
        initialStatus = "All Status";
    }

    private void selectFilterValue(ComboBox<String> filter, String value) {
        if (filter.getItems().contains(value)) {
            filter.getSelectionModel().select(value);
        } else {
            filter.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleItemClick(MouseEvent event) {
        Object item = findReportFromEvent(event);
        if (item instanceof ItemReport report) {
            openItemDetails(event, report);
        }
    }

    private Object findReportFromEvent(MouseEvent event) {
        Node node = event.getPickResult() == null
                ? (Node) event.getSource()
                : event.getPickResult().getIntersectedNode();

        while (node != null) {
            if (node.getUserData() instanceof ItemReport) {
                return node.getUserData();
            }
            node = node.getParent();
        }

        return ((Node) event.getSource()).getUserData();
    }

    private void renderItems() {
        itemsGrid.getChildren().clear();
        configureGridColumns(currentColumnCount);

        List<ItemReport> filteredItems = AppDataStore.getItemReports().stream()
                .filter(this::matchesFilters)
                .toList();

        if (filteredItems.isEmpty()) {
            Label emptyLabel = new Label("No items found.");
            emptyLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 16;");
            itemsGrid.add(emptyLabel, 0, 0, currentColumnCount, 1);
            return;
        }

        for (int index = 0; index < filteredItems.size(); index++) {
            ItemReport item = filteredItems.get(index);
            VBox card = createItemCard(item);
            itemsGrid.add(card, index % currentColumnCount, index / currentColumnCount);
        }
    }

    private void updateColumnsAndRender(double gridWidth) {
        int newColumnCount = calculateColumnCount(gridWidth);
        if (newColumnCount != currentColumnCount) {
            currentColumnCount = newColumnCount;
            renderItems();
        }
    }

    private int calculateColumnCount(double gridWidth) {
        if (gridWidth <= 0) {
            return currentColumnCount;
        }
        return Math.max(1, (int) ((gridWidth + GRID_GAP) / (CARD_MIN_WIDTH + GRID_GAP)));
    }

    private void configureGridColumns(int columnCount) {
        itemsGrid.getColumnConstraints().clear();
        for (int column = 0; column < columnCount; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHgrow(Priority.ALWAYS);
            constraints.setFillWidth(true);
            constraints.setPercentWidth(100.0 / columnCount);
            itemsGrid.getColumnConstraints().add(constraints);
        }
    }

    private boolean matchesFilters(ItemReport item) {
        String search = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String category = categoryFilter.getValue();
        String status = statusFilter.getValue();

        if (isExpired(item)) {
            return false; 
        }

        boolean matchesSearch = search.isEmpty()
                || safe(item.getItemName()).toLowerCase(Locale.ROOT).contains(search)
                || safe(item.getCategory()).toLowerCase(Locale.ROOT).contains(search)
                || safe(item.getLocation()).toLowerCase(Locale.ROOT).contains(search)
                || safe(item.getReportedBy()).toLowerCase(Locale.ROOT).contains(search);

        boolean matchesCategory = "All Categories".equals(category)
                || safe(item.getCategory()).equalsIgnoreCase(category);
        boolean matchesStatus = "All Status".equals(status)
                || safe(item.getType()).equalsIgnoreCase(status);

        return matchesSearch && matchesCategory && matchesStatus;
    }

    private VBox createItemCard(ItemReport item) {
        VBox card = new VBox(8);
        card.setPrefWidth(185);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(205);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 4); -fx-cursor: hand;");
        card.setUserData(item);
        card.setOnMouseClicked(this::handleItemClick);
        GridPane.setHgrow(card, Priority.ALWAYS);

        Label name = new Label(item.getItemName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #4A1515; -fx-font-weight: bold; -fx-font-size: 14;");

        Label badge = new Label(item.getType());
        badge.setAlignment(Pos.CENTER);
        badge.setMinWidth(62);
        
        Label detailsLabel = new Label();
        detailsLabel.setWrapText(true);
        Region imageNode;

        // SECURITY LOGIC: Split UI based on Type!
        if ("Lost".equalsIgnoreCase(item.getType())) {
            badge.setStyle("-fx-background-color: #FFCDD2; -fx-background-radius: 12; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-padding: 3 10 3 10;");
            detailsLabel.setText(item.getDescription()); 
            detailsLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12;");
            imageNode = createRealImageBox(item);        
        } else {
            badge.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 12; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 3 10 3 10;");
            detailsLabel.setText("Details hidden until admin verification");
            detailsLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-font-size: 12;");
            imageNode = createProtectedImageBox();      
        }

        card.getChildren().addAll(imageNode, name, badge, detailsLabel);
        return card;
    }

    private Region createProtectedImageBox() {
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefHeight(92);
        imageBox.setStyle("-fx-background-color: #EFEFEF; -fx-background-radius: 10;");
        Label placeholder = new Label("Image hidden");
        placeholder.setStyle("-fx-text-fill: #999999; -fx-font-weight: bold;");
        imageBox.getChildren().add(placeholder);

        return imageBox;
    }
    
    private Region createRealImageBox(ItemReport item) {
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefHeight(92);
        
        javafx.scene.image.Image img = com.example.findit.util.ImageStorage.loadImage(item.getImagePath());
        if (img != null) {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
            imageView.setFitWidth(160);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);
            imageBox.getChildren().add(imageView);
        } else {
            Label placeholder = new Label("No Image");
            placeholder.setStyle("-fx-text-fill: #999999; -fx-font-weight: bold;");
            imageBox.setStyle("-fx-background-color: #EFEFEF; -fx-background-radius: 10;");
            imageBox.getChildren().add(placeholder);
        }
        return imageBox;
    }

    private void openItemDetails(MouseEvent event, ItemReport item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/findit/views/user/ItemDetails.fxml"));
            Parent root = loader.load();
            ItemDetailsController controller = loader.getController();
            controller.setItem(item);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setTitle("Item Details");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("Could not open item details");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleManageItem(javafx.event.ActionEvent event) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Manage Item Submission");
        dialog.setHeaderText("Secure Access");
        dialog.setContentText("Enter your Item Tracking ID (e.g., AB-1234):");

        dialog.showAndWait().ifPresent(trackingId -> {
            String sanitizedId = trackingId.trim().toUpperCase();

            if (!sanitizedId.matches("[A-Z]{2}-\\d{4}")) {
                com.example.findit.util.toast.show(((javafx.scene.Node) event.getSource()).getScene().getWindow(), "Invalid Format. Must be LL-NNNN.", "error");
                return;
            }

            com.example.findit.model.ItemReport foundItem = com.example.findit.model.AppDataStore.getItemReports().stream()
                    .filter(item -> sanitizedId.equals(item.getTrackingId()))
                    .findFirst()
                    .orElse(null);

            if (foundItem != null) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/findit/views/user/ItemDetails.fxml"));
                    javafx.scene.Parent root = loader.load();
                    
                    ItemDetailsController controller = loader.getController();
                    controller.setItem(foundItem);
                    controller.enableManagerMode();
                    
                    // UPDATED: Now blocks the background and waits for close before refreshing!
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(((Node) event.getSource()).getScene().getWindow());
                    stage.setTitle("Manage Submission");
                    stage.setScene(new javafx.scene.Scene(root));
                    stage.showAndWait(); 
                    
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            } else {
                com.example.findit.util.toast.show(((javafx.scene.Node) event.getSource()).getScene().getWindow(), "No item found with ID: " + sanitizedId, "warning");
            }
        });
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
