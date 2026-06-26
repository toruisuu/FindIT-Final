package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemReport;
import com.example.findit.util.ImageStorage;
import com.example.findit.util.ResponsiveTable;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportedItemsController implements Initializable {

    // INJECTS THE SIDEBAR CONTROLLER!
    @FXML private AdminSidebarController sidebarController;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> viewToggle;
    @FXML private Label lblTimestamp; // NEW: The Live Clock Label
    @FXML private TableView<ItemReport> itemsTable;
    
    @FXML private TableColumn<ItemReport, String> colType, colTrackingId, colItemName, colCategory, colDate, colReportedBy, colLocation, colAction;

    private FilteredList<ItemReport> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) { sidebarController.setActiveTab("Reported"); }
        
        // 1. LIVE CLOCK SETUP
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy  hh:mm:ss a");
            if (lblTimestamp != null) {
                lblTimestamp.setText(LocalDateTime.now().format(formatter));
            }
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // 2. SETUP DROPDOWNS
        typeFilter.setItems(FXCollections.observableArrayList("All", "Lost", "Found"));
        typeFilter.getSelectionModel().selectFirst();
        
        // Setup the new View Toggle
        if (viewToggle != null) {
            viewToggle.setItems(FXCollections.observableArrayList("Active Records", "Archived History"));
            viewToggle.getSelectionModel().selectFirst();
        }

        configureTableColumns();
        ResponsiveTable.fillAvailableWidth(itemsTable);
        wireSearchAndFilter();
        
        // 3. LISTEN FOR TOGGLE CHANGES (Active vs Archived)
        if (viewToggle != null) {
            viewToggle.valueProperty().addListener((obs, oldVal, newVal) -> {
                wireSearchAndFilter(); // Re-wire the table to the new list
                
                if ("Archived History".equals(newVal)) {
                    // Make the table slightly grey to indicate it's the archive
                    itemsTable.setStyle("-fx-control-inner-background: #f4f4f4;"); 
                } else {
                    itemsTable.setStyle("-fx-control-inner-background: #ffffff;");
                }
            });
        }
    }

    private void configureTableColumns() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colReportedBy.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTrackingId.setCellValueFactory(new PropertyValueFactory<>("trackingId"));
        colType.setStyle("-fx-alignment: CENTER;");
        colTrackingId.setStyle("-fx-alignment: CENTER;");
        colAction.setStyle("-fx-alignment: CENTER;");
        
        colType.setCellFactory(col -> new TableCell<ItemReport, String>() {
            private final Label badge = new Label();
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setStyle("-fx-alignment: CENTER;");
                    badge.setText(item);
                    if (item.equalsIgnoreCase("Lost")) {
                        badge.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    } else if (item.equalsIgnoreCase("Found")) {
                        badge.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");
                    }
                    setGraphic(badge);
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<ItemReport, String>() {
            private final Button viewBtn = new Button();
            private final Button deleteBtn = new Button();

            {
                ImageView eyeIcon = createIcon("/com/example/findit/assets/ViewEye.png");
                ImageView trashIcon = createIcon("/com/example/findit/assets/trash.png");

                viewBtn.setGraphic(eyeIcon);
                deleteBtn.setGraphic(trashIcon);
                String transparentStyle = "-fx-background-color: transparent; -fx-cursor: hand;";
                viewBtn.setStyle(transparentStyle);
                deleteBtn.setStyle(transparentStyle);

                viewBtn.setOnAction(e -> {
                    ItemReport item = getTableView().getItems().get(getIndex());
                    handleViewItem(item);
                });

                deleteBtn.setOnAction(e -> {
                    ItemReport item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(8);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER);
                    actionBox.setMaxWidth(Double.MAX_VALUE);
                    
                    actionBox.getChildren().add(viewBtn);
                    boolean isArchived = viewToggle != null && "Archived History".equals(viewToggle.getValue());
                    
                    if (!isArchived) {
                        actionBox.getChildren().add(deleteBtn);
                    }

                    setGraphic(actionBox);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }
    
    private ImageView createIcon(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("X Missing table icon: " + path);
            return new ImageView(); 
        }
        ImageView imgView = new ImageView(new Image(stream));
        imgView.setFitWidth(20);
        imgView.setFitHeight(20);
        imgView.setPreserveRatio(true);
        return imgView;
    }

    private void wireSearchAndFilter() {
        // Determine which list to use based on the viewToggle!
        boolean isArchived = viewToggle != null && "Archived History".equals(viewToggle.getValue());
        ObservableList<ItemReport> sourceList = isArchived ? AppDataStore.ARCHIVED_ITEMS : AppDataStore.getItemReports();

        // Wrap the selected list in a FilteredList
        filteredData = new FilteredList<>(sourceList, p -> true);
        itemsTable.setItems(filteredData);

        // Clear old listeners and add new ones
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        
        applyFilter(); // Apply immediately to refresh
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String typeValue = typeFilter.getValue();

        filteredData.setPredicate(item -> {
            boolean matchesSearch = search.isEmpty()
                    || item.getItemName().toLowerCase().contains(search)
                    || safe(item.getTrackingId()).toLowerCase().contains(search)
                    || item.getCategory().toLowerCase().contains(search)
                    || item.getLocation().toLowerCase().contains(search)
                    || item.getReportedBy().toLowerCase().contains(search);

            boolean matchesType = "All".equals(typeValue)
                    || item.getType().equalsIgnoreCase(typeValue);

            return matchesSearch && matchesType;
        });
    }

    private void handleViewItem(ItemReport item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Item Details");
        dialog.setHeaderText(item.getItemName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(760);

        javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(22);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(10, 5, 5, 5));

        VBox details = new VBox(12);
        details.setPrefWidth(430);
        details.getChildren().addAll(
                createDetailsGrid(item),
                createDescriptionBlock(item)
        );
        javafx.scene.layout.HBox.setHgrow(details, Priority.ALWAYS);

        content.getChildren().addAll(createImagePanel(item), details);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private VBox createImagePanel(ItemReport item) {
        VBox panel = new VBox(8);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(250);

        StackPane imageFrame = new StackPane();
        imageFrame.setPrefSize(250, 230);
        imageFrame.setStyle("-fx-background-color: #F0F0F3; -fx-background-radius: 10;");

        Image image = ImageStorage.loadImage(item.getImagePath());
        if (image == null) {
            Label placeholder = new Label("No Image Available");
            placeholder.setStyle("-fx-text-fill: #777777; -fx-font-weight: bold;");
            imageFrame.getChildren().add(placeholder);
        } else {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(230);
            imageView.setFitHeight(210);
            imageView.setPreserveRatio(true);
            imageFrame.getChildren().add(imageView);
        }

        Label imageLabel = new Label(item.getType() + " Item Image");
        imageLabel.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold;");
        panel.getChildren().addAll(imageFrame, imageLabel);
        return panel;
    }

    private GridPane createDetailsGrid(ItemReport item) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(105);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelColumn, valueColumn);

        addDetailRow(grid, 0, "Tracking ID", item.getTrackingId());
        addDetailRow(grid, 1, "Type", item.getType());
        addDetailRow(grid, 2, "Category", item.getCategory());
        addDetailRow(grid, 3, "Date", item.getDate());
        addDetailRow(grid, 4, "Reported By", item.getReportedBy());
        addDetailRow(grid, 5, "Contact", item.getContact());
        addDetailRow(grid, 6, "Location", item.getLocation());
        return grid;
    }

    private VBox createDescriptionBlock(ItemReport item) {
        Label title = new Label("Description");
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 13;");

        Label description = new Label(safe(item.getDescription()));
        description.setWrapText(true);
        description.setMinHeight(80);
        description.setStyle("-fx-background-color: #F7F7F9; -fx-background-radius: 8; -fx-padding: 10; -fx-text-fill: #333333;");

        VBox block = new VBox(6, title, description);
        return block;
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

    private void handleDeleteItem(ItemReport item) {
        if (viewToggle != null && "Archived History".equals(viewToggle.getValue())) {
            System.out.println("Item is already archived. Action blocked.");
            return; // Instantly stops the method!
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Archive Confirmation");
        confirmDialog.setHeaderText("Dispose / Archive Report: " + item.getItemName());
        confirmDialog.setContentText("Are you sure you want to archive this item? It will be removed from the active board but kept in the database history.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AppDataStore.archiveItemReport(item);
                
                wireSearchAndFilter(); 
            }
        });
    }
}