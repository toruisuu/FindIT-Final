package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemMatch;
import com.example.findit.model.ItemReport;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

import com.example.findit.util.ImageStorage;
import com.example.findit.util.ResponsiveTable;


public class AdminDashboardController implements Initializable {

    @FXML private AdminSidebarController sidebarController;

    @FXML private Label foundItemsCount;
    @FXML private Label lostReportsCount;
    @FXML private Label matchedCount;

    @FXML private ProgressBar pbElectronics, pbWallet, pbDocument;
    @FXML private Label lblElectronics, lblWallet, lblDocument;
    @FXML private VBox topLocationsBox;

    @FXML private TableView<ItemRow> recentItemsTable;

    @FXML private TableColumn<ItemRow, String> colItem;
    @FXML private TableColumn<ItemRow, String> colCategory;
    @FXML private TableColumn<ItemRow, String> colLocation;
    @FXML private TableColumn<ItemRow, String> colStatus;
    @FXML private TableColumn<ItemRow, String> colDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (sidebarController != null) {
            sidebarController.setActiveTab("Dashboard");
        }

        configureTableColumns();
        ResponsiveTable.fillAvailableWidth(recentItemsTable);
        refreshDashboard();
        AppDataStore.getItemReports().addListener((ListChangeListener<ItemReport>) change -> refreshDashboard());
        AppDataStore.getMatchSuggestions().addListener((ListChangeListener<ItemMatch>) change -> refreshDashboard());
    }

    private void configureTableColumns() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        recentItemsTable.setRowFactory(table -> {
            TableRow<ItemRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    showItemDetails(row.getItem().getItem());
                }
            });
            row.setStyle("-fx-cursor: hand;");
            return row;
        });
    }

    private void refreshDashboard() {
        loadDashboardStats();
        loadCategoryBreakdown();
        loadTopLocations();
        loadRecentItems();
    }

    private void loadDashboardStats() {
        foundItemsCount.setText(String.valueOf(AppDataStore.countItemsByType("Found")));
        lostReportsCount.setText(String.valueOf(AppDataStore.countItemsByType("Lost")));
        matchedCount.setText(String.valueOf(AppDataStore.countMatches()));
        foundItemsCount.setStyle("-fx-text-fill: #4A1515;");
        lostReportsCount.setStyle("-fx-text-fill: #4A1515;");
        matchedCount.setStyle("-fx-text-fill: #4A1515;");
    }

    private void loadCategoryBreakdown() {
        List<ItemReport> reports = AppDataStore.getItemReports();
        double total = Math.max(1, reports.size());
        updateCategoryProgress("Electronics", lblElectronics, pbElectronics, total);
        updateCategoryProgress("Wallet", lblWallet, pbWallet, total);
        updateCategoryProgress("Documents", lblDocument, pbDocument, total);
    }

    private void updateCategoryProgress(String category, Label label, ProgressBar progressBar, double total) {
        long count = AppDataStore.getItemReports().stream()
                .filter(item -> category.equalsIgnoreCase(safe(item.getCategory())))
                .count();
        label.setText(count + (count == 1 ? " item" : " items"));
        label.setStyle("-fx-text-fill: #777777;");
        progressBar.setProgress(count / total);
    }

    private void loadTopLocations() {
        if (topLocationsBox == null) {
            return;
        }

        topLocationsBox.getChildren().clear();
        Map<String, Long> topLocations = AppDataStore.getItemReports().stream()
                .collect(Collectors.groupingBy(
                        item -> displayValue(item.getLocation(), "Unknown location"),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        if (topLocations.isEmpty()) {
            Label empty = new Label("No locations yet");
            empty.setStyle("-fx-text-fill: #777777;");
            topLocationsBox.getChildren().add(empty);
            return;
        }

        topLocations.forEach((location, count) -> {
            Label locationLabel = new Label(location);
            locationLabel.setStyle("-fx-text-fill: #800000;");
            Label countLabel = new Label(count + (count == 1 ? " item" : " items"));
            countLabel.setStyle("-fx-text-fill: #777777;");
            HBox row = new HBox(locationLabel, new Region(), countLabel);
            HBox.setHgrow(row.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
            topLocationsBox.getChildren().add(row);
        });
    }

    private void loadRecentItems() {
        ObservableList<ItemRow> data = FXCollections.observableArrayList(
                AppDataStore.getItemReports().stream()
                        .limit(10)
                        .map(item -> new ItemRow(
                                item,
                                displayValue(item.getItemName(), "Unnamed item"),
                                displayValue(item.getCategory(), "Uncategorized"),
                                displayValue(item.getLocation(), "Unknown location"),
                                displayValue(item.getType(), "Unknown"),
                                displayValue(item.getDate(), "-")
                        ))
                        .toList()
        );
        recentItemsTable.setItems(data);
    }

    private void showItemDetails(ItemReport item) {
        if (item == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getItemName());

        HBox content = new HBox(24);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(8, 0, 0, 0));

        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefSize(220, 190);
        imageBox.setMinSize(220, 190);
        imageBox.setStyle("-fx-background-color: #F1F1F1; -fx-background-radius: 10;");

        Image image = ImageStorage.loadImage(item.getImagePath());
        if (image == null) {
            Label placeholder = new Label("No image");
            placeholder.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");
            imageBox.getChildren().add(placeholder);
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(205);
            imageView.setFitHeight(175);
            imageView.setPreserveRatio(true);
            imageBox.getChildren().add(imageView);
        }

        GridPane details = new GridPane();
        details.setHgap(18);
        details.setVgap(10);
        details.add(detailBlock("Type", item.getType()), 0, 0);
        details.add(detailBlock("Category", item.getCategory()), 1, 0);
        details.add(detailBlock("Date", item.getDate()), 0, 1);
        details.add(detailBlock("Location", item.getLocation()), 1, 1);
        details.add(detailBlock("Reported By", item.getReportedBy()), 0, 2);
        details.add(detailBlock("Contact", item.getContact()), 1, 2);
        details.add(detailBlock("Description", item.getDescription()), 0, 3, 2, 1);

        content.getChildren().addAll(imageBox, details);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(720);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private VBox detailBlock(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #888888;");
        Label valueNode = new Label(displayValue(value, "-"));
        valueNode.setWrapText(true);
        valueNode.setStyle("-fx-text-fill: #222222; -fx-font-weight: bold;");
        VBox box = new VBox(2, labelNode, valueNode);
        box.setPrefWidth(190);
        return box;
    }

    private String displayValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static class ItemRow {

        private final ItemReport item;
        private final String itemName;
        private final String category;
        private final String location;
        private final String status;
        private final String date;

        public ItemRow(ItemReport item,
                       String itemName,
                       String category,
                       String location,
                       String status,
                       String date) {

            this.item = item;
            this.itemName = itemName;
            this.category = category;
            this.location = location;
            this.status = status;
            this.date = date;
        }

        public ItemReport getItem() {
            return item;
        }

        public String getItemName() {
            return itemName;
        }

        public String getCategory() {
            return category;
        }

        public String getLocation() {
            return location;
        }

        public String getStatus() {
            return status;
        }

        public String getDate() {
            return date;
        }
    }
}
