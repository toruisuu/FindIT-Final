package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemReport;
import com.example.findit.util.ResponsiveTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MonitoringController implements Initializable {

    @FXML private AdminSidebarController sidebarController;

    @FXML private Label lblTotalItems;
    @FXML private Label lblLostItems;
    @FXML private Label lblFoundItems;
    @FXML private Label lblPendingClaims;

    @FXML private TableView<ItemRow> itemTable;
    @FXML private TableColumn<ItemRow, String> colTrackingId;
    @FXML private TableColumn<ItemRow, String> colStatus;
    @FXML private TableColumn<ItemRow, String> colItemName;
    @FXML private TableColumn<ItemRow, String> colCategory;
    @FXML private TableColumn<ItemRow, String> colDate;
    @FXML private TableColumn<ItemRow, String> colLocation;
    @FXML private TableColumn<ItemRow, String> colReportedBy;
    @FXML private TableColumn<ItemRow, String> colContact;
    @FXML private TableColumn<ItemRow, String> colDescription;

    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> dateFilter;
    @FXML private DatePicker monthPicker;
    @FXML private TextField itemSearch;

    private final ObservableList<ItemRow> itemMaster = FXCollections.observableArrayList();
    private FilteredList<ItemRow> itemFiltered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) {
            sidebarController.setActiveTab("Monitoring");
        }
        configureItemTable();
        setupFilters();
        refreshItems();
        AppDataStore.getItemReports().addListener((javafx.collections.ListChangeListener<ItemReport>) change -> refreshItems());
    }

    private void configureItemTable() {
        colTrackingId.setCellValueFactory(new PropertyValueFactory<>("trackingId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colReportedBy.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                String style = "Lost".equalsIgnoreCase(item)
                        ? "-fx-background-color: #FFCDD2; -fx-text-fill: #C62828;"
                        : "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32;";
                badge.setStyle(style + " -fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");
                setGraphic(badge);
            }
        });

        ResponsiveTable.fillAvailableWidth(itemTable);
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList("All Items", "Lost", "Found"));
        dateFilter.setItems(FXCollections.observableArrayList(
                "All Dates", "Today", "A Day Ago", "A Week Ago", "A Year Ago", "Specific Month"
        ));
        typeFilter.getSelectionModel().selectFirst();
        dateFilter.getSelectionModel().selectFirst();

        itemFiltered = new FilteredList<>(itemMaster, row -> true);
        itemTable.setItems(itemFiltered);

        itemSearch.textProperty().addListener((obs, oldValue, newValue) -> applyItemFilter());
        typeFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyItemFilter());
        dateFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            updateMonthPickerVisibility();
            applyItemFilter();
        });
        monthPicker.valueProperty().addListener((obs, oldValue, newValue) -> applyItemFilter());
        updateMonthPickerVisibility();
    }

    private void refreshItems() {
        itemMaster.setAll(AppDataStore.getItemReports().stream()
                .map(ItemRow::new)
                .toList());
        lblTotalItems.setText(String.valueOf(AppDataStore.getItemReports().size()));
        lblLostItems.setText(String.valueOf(AppDataStore.countItemsByType("Lost")));
        lblFoundItems.setText(String.valueOf(AppDataStore.countItemsByType("Found")));
        lblPendingClaims.setText(String.valueOf(AppDataStore.countClaimsByStatus("Pending")));
        applyItemFilter();
    }

    private void applyItemFilter() {
        if (itemFiltered == null) {
            return;
        }
        String search = itemSearch.getText() == null ? "" : itemSearch.getText().trim().toLowerCase(Locale.ROOT);
        String type = typeFilter.getValue();

        itemFiltered.setPredicate(row -> {
            boolean matchesType = type == null || "All Items".equals(type)
                    || row.getStatus().equalsIgnoreCase(type);
            boolean matchesSearch = search.isEmpty()
                    || row.getTrackingId().toLowerCase(Locale.ROOT).contains(search)
                    || row.getItemName().toLowerCase(Locale.ROOT).contains(search)
                    || row.getCategory().toLowerCase(Locale.ROOT).contains(search)
                    || row.getLocation().toLowerCase(Locale.ROOT).contains(search)
                    || row.getReportedBy().toLowerCase(Locale.ROOT).contains(search)
                    || row.getDescription().toLowerCase(Locale.ROOT).contains(search);
            return matchesType && matchesSearch && matchesDate(row);
        });
    }

    private boolean matchesDate(ItemRow row) {
        String filter = dateFilter.getValue();
        if (filter == null || "All Dates".equals(filter)) {
            return true;
        }

        LocalDate itemDate = parseDate(row.getDate());
        if (itemDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return switch (filter) {
            case "Today" -> itemDate.isEqual(today);
            case "A Day Ago" -> itemDate.isEqual(today.minusDays(1));
            case "A Week Ago" -> !itemDate.isBefore(today.minusWeeks(1));
            case "A Year Ago" -> !itemDate.isBefore(today.minusYears(1));
            case "Specific Month" -> matchesSelectedMonth(itemDate);
            default -> true;
        };
    }

    private boolean matchesSelectedMonth(LocalDate itemDate) {
        LocalDate selectedDate = monthPicker.getValue();
        if (selectedDate == null) {
            return true;
        }
        return YearMonth.from(itemDate).equals(YearMonth.from(selectedDate));
    }

    private LocalDate parseDate(String date) {
        try {
            return date == null || date.isBlank() ? null : LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void updateMonthPickerVisibility() {
        boolean showMonth = "Specific Month".equals(dateFilter.getValue());
        monthPicker.setVisible(showMonth);
        monthPicker.setManaged(showMonth);
    }

    @FXML
    private void handleRefresh() {
        refreshItems();
    }

    public static class ItemRow {
        private final ItemReport item;

        public ItemRow(ItemReport item) {
            this.item = item;
        }

        public String getTrackingId() { return display(item.getTrackingId()); }
        public String getStatus() { return display(item.getType()); }
        public String getItemName() { return display(item.getItemName()); }
        public String getCategory() { return display(item.getCategory()); }
        public String getDate() { return display(item.getDate()); }
        public String getLocation() { return display(item.getLocation()); }
        public String getReportedBy() { return display(item.getReportedBy()); }
        public String getContact() { return display(item.getContact()); }
        public String getDescription() { return display(item.getDescription()); }

        private static String display(String value) {
            return value == null || value.isBlank() ? "N/A" : value;
        }
    }
}
