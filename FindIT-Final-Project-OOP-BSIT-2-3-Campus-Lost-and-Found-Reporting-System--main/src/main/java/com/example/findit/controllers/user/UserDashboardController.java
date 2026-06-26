package com.example.findit.controllers.user;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemReport;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class UserDashboardController {

    @FXML private TextField txtSearch;
    @FXML private ListView<ItemReport> searchSuggestions;
    @FXML private HBox filterPanel;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    
    @FXML
    public void initialize() {
        UserSidebarController.setActivePage("Dashboard");
        setupDashboardFilters();
        if (txtSearch != null) {
            txtSearch.setOnAction(e -> goToItemsFromSearch());
            txtSearch.textProperty().addListener((obs, oldValue, newValue) -> updateSearchSuggestions(newValue));
            AppDataStore.getItemReports().addListener((javafx.collections.ListChangeListener<ItemReport>) change ->
                    updateSearchSuggestions(txtSearch.getText()));
        }
        configureEnterSearch(categoryFilter);
        configureEnterSearch(statusFilter);
        if (searchSuggestions != null) {
            searchSuggestions.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(ItemReport item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getItemName() + " (" + item.getType() + ") - " + item.getLocation());
                    }
                }
            });
            searchSuggestions.setVisible(false);
            searchSuggestions.setManaged(false);
        }
    }

    @FXML
    public void handleReportLost(ActionEvent event) {
        UserNavigationHelper.switchScene(event, "/com/example/findit/views/user/LostForm.fxml");
    }

    @FXML
    public void handleReportFound(ActionEvent event) {
        UserNavigationHelper.switchScene(event, "/com/example/findit/views/user/FoundForm.fxml");
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        if (filterPanel == null) {
            return;
        }

        boolean shouldShow = !filterPanel.isVisible();
        filterPanel.setVisible(shouldShow);
        filterPanel.setManaged(shouldShow);
        updateSearchSuggestions(txtSearch == null ? "" : txtSearch.getText());
    }

    private void goToItemsFromSearch() {
        UserSidebarController.setActivePage("Items");
        ItemsController.openWithFilters(
                txtSearch == null ? "" : txtSearch.getText(),
                categoryFilter == null ? "All Categories" : categoryFilter.getValue(),
                statusFilter == null ? "All Status" : statusFilter.getValue()
        );
        if (txtSearch != null && txtSearch.getScene() != null) {
            UserNavigationHelper.switchScene(txtSearch, "/com/example/findit/views/user/Items.fxml");
        }
    }

    private void configureEnterSearch(ComboBox<String> filter) {
        if (filter == null) {
            return;
        }

        filter.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                goToItemsFromSearch();
                event.consume();
            }
        });
    }

    private void updateSearchSuggestions(String query) {
        if (searchSuggestions == null) {
            return;
        }

        String searchText = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (searchText.isEmpty()) {
            hideSearchSuggestions();
            return;
        }

        var matches = AppDataStore.getItemReports().stream()
                .filter(item -> matchesSearch(item, searchText))
                .filter(this::matchesDashboardFilters)
                .limit(8)
                .toList();

        searchSuggestions.setItems(FXCollections.observableArrayList(matches));
        boolean hasMatches = !matches.isEmpty();
        searchSuggestions.setVisible(hasMatches);
        searchSuggestions.setManaged(hasMatches);
        searchSuggestions.setPrefHeight(Math.min(220, Math.max(48, matches.size() * 42)));
    }

    private boolean matchesSearch(ItemReport item, String searchText) {
        return safe(item.getItemName()).toLowerCase(Locale.ROOT).contains(searchText)
                || safe(item.getCategory()).toLowerCase(Locale.ROOT).contains(searchText)
                || safe(item.getLocation()).toLowerCase(Locale.ROOT).contains(searchText)
                || safe(item.getType()).toLowerCase(Locale.ROOT).contains(searchText);
    }

    private void setupDashboardFilters() {
        if (categoryFilter != null) {
            categoryFilter.setItems(FXCollections.observableArrayList(
                    "All Categories", "Electronics", "Wallet", "Documents", "Clothing", "Accessories", "Other"
            ));
            categoryFilter.getSelectionModel().selectFirst();
            categoryFilter.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateSearchSuggestions(txtSearch == null ? "" : txtSearch.getText()));
        }

        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("All Status", "Lost", "Found"));
            statusFilter.getSelectionModel().selectFirst();
            statusFilter.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateSearchSuggestions(txtSearch == null ? "" : txtSearch.getText()));
        }

        if (filterPanel != null) {
            filterPanel.setVisible(false);
            filterPanel.setManaged(false);
        }
    }

    private boolean matchesDashboardFilters(ItemReport item) {
        String category = categoryFilter == null ? "All Categories" : categoryFilter.getValue();
        String status = statusFilter == null ? "All Status" : statusFilter.getValue();

        boolean matchesCategory = category == null || "All Categories".equals(category)
                || safe(item.getCategory()).equalsIgnoreCase(category);
        boolean matchesStatus = status == null || "All Status".equals(status)
                || safe(item.getType()).equalsIgnoreCase(status);

        return matchesCategory && matchesStatus;
    }

    @FXML
    private void handleSuggestionClick(MouseEvent event) {
        if (event.getClickCount() < 1 || searchSuggestions == null) {
            return;
        }

        ItemReport selectedItem = searchSuggestions.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            openItemDetails(selectedItem);
        }
    }

    private void openItemDetails(ItemReport item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/findit/views/user/ItemDetails.fxml"));
            Parent root = loader.load();
            ItemDetailsController controller = loader.getController();
            controller.setItem(item);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(txtSearch.getScene().getWindow());
            dialog.setTitle("Item Details");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("Could not open item details from dashboard search.");
            e.printStackTrace();
        }
    }

    private void hideSearchSuggestions() {
        searchSuggestions.getItems().clear();
        searchSuggestions.setVisible(false);
        searchSuggestions.setManaged(false);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
