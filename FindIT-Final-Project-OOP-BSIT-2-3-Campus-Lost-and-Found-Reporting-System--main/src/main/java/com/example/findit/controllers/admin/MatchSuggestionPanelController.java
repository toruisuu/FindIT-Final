package com.example.findit.controllers.admin;

import com.example.findit.model.AppDataStore;
import com.example.findit.model.ItemMatch;
import com.example.findit.model.ItemReport;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MatchSuggestionPanelController implements Initializable {
    @FXML private AdminSidebarController sidebarController;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox matchCardsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (sidebarController != null) {
            sidebarController.setActiveTab("Match");
        }

        statusFilter.setItems(FXCollections.observableArrayList("All Status", "Pending", "Confirmed"));
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> renderMatches());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderMatches());
        AppDataStore.getMatchSuggestions().addListener((javafx.collections.ListChangeListener<ItemMatch>) change -> renderMatches());
        renderMatches();
    }

    private void renderMatches() {
        matchCardsContainer.getChildren().clear();

        var matches = AppDataStore.getMatchSuggestions().stream()
                .filter(this::matchesFilters)
                .toList();

        if (matches.isEmpty()) {
            Label emptyLabel = new Label("No matching lost and found reports yet.");
            emptyLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 15;");
            matchCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (ItemMatch match : matches) {
            matchCardsContainer.getChildren().add(createMatchCard(match));
        }
    }

    private boolean matchesFilters(ItemMatch match) {
        String search = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter.getValue();

        boolean matchesSearch = search.isEmpty()
                || searchableText(match.getLostItem()).contains(search)
                || searchableText(match.getFoundItem()).contains(search);
        boolean matchesStatus = "All Status".equals(status)
                || match.getStatus().equalsIgnoreCase(status);

        return matchesSearch && matchesStatus;
    }

    private String searchableText(ItemReport item) {
        return (safe(item.getItemName()) + " "
                + safe(item.getCategory()) + " "
                + safe(item.getLocation()) + " "
                + safe(item.getReportedBy())).toLowerCase(Locale.ROOT);
    }

    private VBox createMatchCard(ItemMatch match) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label title = new Label("Potential Match");
        title.setStyle("-fx-text-fill: #4A1212; -fx-font-weight: bold; -fx-font-size: 18;");

        Label statusBadge = createStatusBadge(match.getStatus());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, statusBadge);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox comparison = new HBox(20, createItemSummary(match.getLostItem()), createItemSummary(match.getFoundItem()));
        Button detailButton = new Button("View Match Details");
        detailButton.setMaxWidth(Double.MAX_VALUE);
        detailButton.setPrefHeight(40);
        detailButton.setStyle("-fx-background-color: #800000; -fx-background-radius: 8; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
        detailButton.setOnAction(event -> openMatchDetail(match));

        card.getChildren().addAll(header, comparison, detailButton);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label statusBadge = new Label(status);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.setPrefHeight(24);
        statusBadge.setPrefWidth("Confirmed".equalsIgnoreCase(status) ? 90 : 80);

        if ("Confirmed".equalsIgnoreCase(status)) {
            statusBadge.setStyle("-fx-background-color: #E8EAF6; -fx-background-radius: 12; -fx-text-fill: #3F51B5; -fx-font-weight: bold; -fx-font-size: 12;");
        } else {
            statusBadge.setStyle("-fx-background-color: #FFE0B2; -fx-background-radius: 12; -fx-text-fill: #E65100; -fx-font-weight: bold; -fx-font-size: 12;");
        }
        return statusBadge;
    }

    private VBox createItemSummary(ItemReport item) {
        VBox box = new VBox(4);
        box.setPrefHeight(95);
        box.setPrefWidth(300);
        box.setPadding(new Insets(10, 15, 10, 15));
        box.setStyle("Lost".equalsIgnoreCase(item.getType())
                ? "-fx-border-color: #FFCDD2; -fx-border-radius: 8; -fx-border-width: 1.5;"
                : "-fx-border-color: #A5D6A7; -fx-border-radius: 8; -fx-border-width: 1.5;");
        HBox.setHgrow(box, Priority.ALWAYS);

        Label type = new Label(item.getType().toUpperCase(Locale.ROOT) + " ITEM");
        type.setStyle("Lost".equalsIgnoreCase(item.getType())
                ? "-fx-text-fill: #E53935; -fx-font-weight: bold; -fx-font-size: 10;"
                : "-fx-text-fill: #43A047; -fx-font-weight: bold; -fx-font-size: 10;");
        Label name = new Label(item.getItemName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #222222; -fx-font-weight: bold; -fx-font-size: 14;");
        Label reporter = new Label("Reported by: " + safe(item.getReportedBy()));
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
            dialog.initOwner(matchCardsContainer.getScene().getWindow());
            dialog.setTitle("Match Details");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("Could not load MatchSuggestion.fxml modal.");
            e.printStackTrace();
        }
    }

    @FXML
    private void openMatchDetail() {
        AppDataStore.getMatchSuggestions().stream().findFirst().ifPresent(this::openMatchDetail);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
