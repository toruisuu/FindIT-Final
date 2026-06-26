package com.example.findit.controllers.admin;

import com.example.findit.model.SessionManager;
import com.example.findit.util.AppWindow;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminSidebarController {
    private static final double EXPANDED_WIDTH = 220.0;
    private static final double COLLAPSED_WIDTH = 75.0;
    private static final double EXPANDED_ICON_GAP = 15.0;

    private static String activeTab = "Dashboard";
    private static boolean isSidebarExpanded = true;

    @FXML private VBox sidebarContainer;
    @FXML private HBox headerBox;
    @FXML private Region headerSpacer;
    @FXML private Label logoText;
    @FXML private Button btnDashboard, btnReported, btnClaims, btnMatch, btnMonitoring, btnLogout;
    @FXML private ImageView logoImage;
    @FXML private ImageView imgDashboard, imgReported, imgClaims, imgMatch, imgMonitoring, imgLogout;

    @FXML
    public void initialize() {
        if (btnDashboard == null) {
            return;
        }
        applySidebarState();
        setActiveTab(activeTab);
    }

    @FXML
    private void toggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded;
        applySidebarState();
    }

    private void applySidebarState() {
        if (isSidebarExpanded) {
            sidebarContainer.setPrefWidth(EXPANDED_WIDTH);
            sidebarContainer.setMinWidth(EXPANDED_WIDTH);
            sidebarContainer.setMaxWidth(EXPANDED_WIDTH);

            logoText.setVisible(true);
            logoText.setManaged(true);
            logoImage.setVisible(true);
            logoImage.setManaged(true);
            headerSpacer.setVisible(true);
            headerSpacer.setManaged(true);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            setNavButtonExpanded(btnDashboard);
            setNavButtonExpanded(btnReported);
            setNavButtonExpanded(btnClaims);
            setNavButtonExpanded(btnMatch);
            setNavButtonExpanded(btnMonitoring);
            setNavButtonExpanded(btnLogout);
        } else {
            sidebarContainer.setPrefWidth(COLLAPSED_WIDTH);
            sidebarContainer.setMinWidth(COLLAPSED_WIDTH);
            sidebarContainer.setMaxWidth(COLLAPSED_WIDTH);

            logoText.setVisible(false);
            logoText.setManaged(false);
            logoImage.setVisible(false);
            logoImage.setManaged(false);
            headerSpacer.setVisible(false);
            headerSpacer.setManaged(false);
            headerBox.setAlignment(Pos.CENTER);

            setNavButtonCollapsed(btnDashboard);
            setNavButtonCollapsed(btnReported);
            setNavButtonCollapsed(btnClaims);
            setNavButtonCollapsed(btnMatch);
            setNavButtonCollapsed(btnMonitoring);
            setNavButtonCollapsed(btnLogout);
        }

        setActiveTab(activeTab);
    }

    private void setNavButtonExpanded(Button button) {
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(EXPANDED_ICON_GAP);
        button.setPadding(new Insets(0, 0, 0, 20));
        button.setPrefWidth(EXPANDED_WIDTH);
        button.setMinWidth(EXPANDED_WIDTH);
        button.setMaxWidth(EXPANDED_WIDTH);
    }

    private void setNavButtonCollapsed(Button button) {
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setAlignment(Pos.CENTER);
        button.setGraphicTextGap(0);
        button.setPadding(Insets.EMPTY);
        button.setPrefWidth(COLLAPSED_WIDTH);
        button.setMinWidth(COLLAPSED_WIDTH);
        button.setMaxWidth(COLLAPSED_WIDTH);
    }

    public void setActiveTab(String tabName) {
        activeTab = tabName;
        String defaultPadding = isSidebarExpanded ? "0 0 0 20" : "0";
        String defaultStyle = "-fx-background-color: transparent; -fx-background-insets: 0; "
                + "-fx-text-fill: #FFFFFF; -fx-cursor: hand; -fx-border-color: transparent; "
                + "-fx-border-insets: 0; -fx-padding: " + defaultPadding + "; "
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        String activeStyle = isSidebarExpanded
                ? "-fx-background-color: transparent; -fx-background-insets: 0; -fx-text-fill: #FFCC00; "
                + "-fx-cursor: hand; -fx-border-color: transparent transparent transparent #FFCC00; "
                + "-fx-border-width: 0 0 0 3; -fx-border-insets: 0; -fx-padding: 0 0 0 20; "
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;"
                : "-fx-background-color: transparent; -fx-background-insets: 0; -fx-text-fill: #FFCC00; "
                + "-fx-cursor: hand; -fx-border-color: transparent; -fx-border-insets: 0; "
                + "-fx-padding: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        btnDashboard.setStyle(defaultStyle);
        btnReported.setStyle(defaultStyle);
        btnClaims.setStyle(defaultStyle);
        btnMatch.setStyle(defaultStyle);
        btnMonitoring.setStyle(defaultStyle);
        btnLogout.setStyle(defaultStyle);

        imgDashboard.setImage(safeLoadImage("/com/example/findit/assets/dashboard.png"));
        imgReported.setImage(safeLoadImage("/com/example/findit/assets/ItemReportedAdmin.png"));
        imgClaims.setImage(safeLoadImage("/com/example/findit/assets/claim.png"));
        imgMatch.setImage(safeLoadImage("/com/example/findit/assets/MatchSuggestion.png"));
        imgMonitoring.setImage(safeLoadImage("/com/example/findit/assets/reportsidebar.png"));
        imgLogout.setImage(safeLoadImage("/com/example/findit/assets/logout.png"));

        ColorAdjust makeWhite = new ColorAdjust();
        makeWhite.setBrightness(1.0);
        imgDashboard.setEffect(makeWhite);
        imgReported.setEffect(makeWhite);
        imgClaims.setEffect(makeWhite);
        imgMatch.setEffect(makeWhite);
        imgMonitoring.setEffect(makeWhite);
        imgLogout.setEffect(makeWhite);

        switch (tabName) {
            case "Dashboard":
                activateTab(btnDashboard, imgDashboard, "/com/example/findit/assets/yellow_icons/dashboard.png", activeStyle);
                break;
            case "Reported":
                activateTab(btnReported, imgReported, "/com/example/findit/assets/yellow_icons/ItemReportedAdmin.png", activeStyle);
                break;
            case "Claims":
                activateTab(btnClaims, imgClaims, "/com/example/findit/assets/yellow_icons/ClaimsAdmin.png", activeStyle);
                break;
            case "Match":
                activateTab(btnMatch, imgMatch, "/com/example/findit/assets/yellow_icons/MatchSuggestion.png", activeStyle);
                break;
            case "Monitoring":
                activateTab(btnMonitoring, imgMonitoring, "/com/example/findit/assets/yellow_icons/report.png", activeStyle);
                break;
            default:
                break;
        }
    }

    private void activateTab(Button button, ImageView icon, String iconPath, String style) {
        button.setStyle(style);
        icon.setImage(safeLoadImage(iconPath));
        icon.setEffect(null);
    }

    @FXML private void goToDashboard(ActionEvent event) {
        navigateTo(event, "/com/example/findit/views/admin/AdminDashboard.fxml", "Dashboard", "Dashboard");
    }

    @FXML private void goToReportedItems(ActionEvent event) {
        navigateTo(event, "/com/example/findit/views/admin/ReportedItems.fxml", "Reported Items", "Reported");
    }

    @FXML private void goToClaims(ActionEvent event) {
        navigateTo(event, "/com/example/findit/views/admin/Claims.fxml", "Claims", "Claims");
    }

    @FXML private void goToMatchSuggestions(ActionEvent event) {
        navigateTo(event, "/com/example/findit/views/admin/MatchSuggestionPanel.fxml", "Match Suggestions", "Match");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.checkOut("Main Portal");
        navigateTo(event, "/com/example/findit/views/user/MainPortal.fxml", "Campus Lost and Found System", "");
    }

    @FXML
    private void goToMonitoring(ActionEvent event) {
        navigateTo(event, "/com/example/findit/views/admin/Monitoring.fxml", "Monitoring", "Monitoring");
    }

    private void navigateTo(ActionEvent event, String fxmlPath, String title, String tabName) {
        if (!tabName.isBlank()) {
            setActiveTab(tabName);
        }
        Platform.runLater(() -> loadScene(event, fxmlPath, title));
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            AppWindow.setRoot(stage, root, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Image safeLoadImage(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Missing image: " + path);
            return null;
        }
        return new Image(stream);
    }
}
