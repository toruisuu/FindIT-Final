package com.example.findit.controllers.user;

import com.example.findit.model.SessionManager;
import com.example.findit.util.AppWindow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

import java.io.IOException;

public class UserSidebarController {

    private static final double EXPANDED_WIDTH = 220.0;
    private static final double COLLAPSED_WIDTH = 75.0;
    private static final double EXPANDED_ICON_GAP = 15.0;

    private static String activePage = "Dashboard";
    private static boolean isSidebarExpanded = true;

    @FXML private VBox sidebarContainer;
    @FXML private HBox headerBox;
    @FXML private Region headerSpacer;
    @FXML private ImageView logoImage;
    @FXML private Label logoText;
    @FXML private Button btnToggleSidebar;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavItems;
    @FXML private Button btnNavClaims;
    @FXML private Button btnNavHelp;
    @FXML private Button btnLogout;

    @FXML private ImageView imgNavDashboard;
    @FXML private ImageView imgNavItems;
    @FXML private ImageView imgNavClaims;
    @FXML private ImageView imgNavHelp;
    @FXML private ImageView imgLogout;

    @FXML
    public void initialize() {
        if (btnNavDashboard == null) {
            return;
        }
        resetSidebarStyles();
        highlightActiveTab();
        applySidebarState();
    }

    public static void setActivePage(String page) {
        activePage = page;
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

            setNavButtonExpanded(btnNavDashboard);
            setNavButtonExpanded(btnNavItems);
            setNavButtonExpanded(btnNavClaims);
            setNavButtonExpanded(btnNavHelp);
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

            setNavButtonCollapsed(btnNavDashboard);
            setNavButtonCollapsed(btnNavItems);
            setNavButtonCollapsed(btnNavClaims);
            setNavButtonCollapsed(btnNavHelp);
            setNavButtonCollapsed(btnLogout);
        }

        highlightActiveTab();
    }

    private void setNavButtonExpanded(Button button) {
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(EXPANDED_ICON_GAP);
        button.setPrefWidth(EXPANDED_WIDTH);
        button.setMinWidth(EXPANDED_WIDTH);
        button.setMaxWidth(EXPANDED_WIDTH);
    }

    private void setNavButtonCollapsed(Button button) {
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setAlignment(Pos.CENTER);
        button.setGraphicTextGap(0);
        button.setPrefWidth(COLLAPSED_WIDTH);
        button.setMinWidth(COLLAPSED_WIDTH);
        button.setMaxWidth(COLLAPSED_WIDTH);
    }

    private Image safeLoadImage(String path) {
        java.io.InputStream resourceStream = getClass().getResourceAsStream(path);
        if (resourceStream == null) {
            System.err.println("Missing image: " + path);
            return null;
        }
        return new Image(resourceStream);
    }

    private void resetSidebarStyles() {
        String padding = isSidebarExpanded ? "0 0 0 20" : "0";
        String defaultStyle = "-fx-background-color: transparent; -fx-background-insets: 0; "
                + "-fx-text-fill: #FFFFFF; -fx-cursor: hand; -fx-border-color: transparent; "
                + "-fx-border-insets: 0; -fx-padding: " + padding + "; "
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
        
        btnNavDashboard.setStyle(defaultStyle);
        btnNavItems.setStyle(defaultStyle);
        btnNavClaims.setStyle(defaultStyle);
        btnNavHelp.setStyle(defaultStyle);
        btnLogout.setStyle(defaultStyle);

        imgNavDashboard.setImage(safeLoadImage("/com/example/findit/assets/dashboard.png"));
        imgNavItems.setImage(safeLoadImage("/com/example/findit/assets/Items.png"));
        imgNavClaims.setImage(safeLoadImage("/com/example/findit/assets/reportsidebar.png"));
        imgNavHelp.setImage(safeLoadImage("/com/example/findit/assets/help.png"));
        imgLogout.setImage(safeLoadImage("/com/example/findit/assets/logout.png"));

        ColorAdjust makeWhite = new ColorAdjust();
        makeWhite.setBrightness(1.0);
        imgNavDashboard.setEffect(makeWhite);
        imgNavItems.setEffect(makeWhite);
        imgNavClaims.setEffect(makeWhite);
        imgNavHelp.setEffect(makeWhite);
        imgLogout.setEffect(makeWhite);
    }

    private void highlightActiveTab() {
        resetSidebarStyles();

        if (isSidebarExpanded) {
            applyExpandedActiveStyle();
        } else {
            applyCollapsedActiveStyle();
        }
    }

    private void applyExpandedActiveStyle() {
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #FFCC00; -fx-cursor: hand; "
                + "-fx-background-insets: 0; -fx-border-color: transparent transparent transparent #FFCC00; "
                + "-fx-border-width: 0 0 0 3; -fx-border-insets: 0; -fx-padding: 0 0 0 20; "
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        switch (activePage) {
            case "Dashboard": 
                activateTab(btnNavDashboard, imgNavDashboard, "/com/example/findit/assets/yellow_icons/dashboard.png", activeStyle);
                break;
            case "Items": 
                activateTab(btnNavItems, imgNavItems, "/com/example/findit/assets/yellow_icons/items.png", activeStyle);
                break;
            case "Claims": 
                activateTab(btnNavClaims, imgNavClaims, "/com/example/findit/assets/yellow_icons/report.png", activeStyle);
                break;
            case "Help": 
                activateTab(btnNavHelp, imgNavHelp, "/com/example/findit/assets/yellow_icons/help.png", activeStyle);
                break;
            default: break;
        }
    }

    private void applyCollapsedActiveStyle() {
        String activeStyle = "-fx-background-color: transparent; -fx-background-insets: 0; "
                + "-fx-cursor: hand; -fx-border-color: transparent; -fx-border-insets: 0; "
                + "-fx-padding: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;";

        switch (activePage) {
            case "Dashboard": 
                activateTab(btnNavDashboard, imgNavDashboard, "/com/example/findit/assets/yellow_icons/dashboard.png", activeStyle);
                break;
            case "Items": 
                activateTab(btnNavItems, imgNavItems, "/com/example/findit/assets/yellow_icons/items.png", activeStyle);
                break;
            case "Claims": 
                activateTab(btnNavClaims, imgNavClaims, "/com/example/findit/assets/yellow_icons/report.png", activeStyle);
                break;
            case "Help": 
                activateTab(btnNavHelp, imgNavHelp, "/com/example/findit/assets/yellow_icons/help.png", activeStyle);
                break;
            default: break;
        }
    }

    private void activateTab(Button button, ImageView icon, String yellowIconPath, String style) {
        button.setStyle(style);
        icon.setImage(safeLoadImage(yellowIconPath));
        icon.setEffect(null); // Removes the white effect to show the yellow
    }

    @FXML
    public void goToDashboard(ActionEvent event) {
        activePage = "Dashboard";
        switchScene(event, "/com/example/findit/views/user/Dashboard.fxml");
    }

    @FXML
    public void goToItems(ActionEvent event) {
        activePage = "Items";
        switchScene(event, "/com/example/findit/views/user/Items.fxml");
    }

    @FXML
    public void goToClaims(ActionEvent event) {
        activePage = "Claims";
        switchScene(event, "/com/example/findit/views/user/Claims.fxml");
    }

    @FXML
    public void goToHelpPage(ActionEvent event) {
        activePage = "Help";
        switchScene(event, "/com/example/findit/views/user/Help.fxml");
    }

    @FXML
    public void logout(ActionEvent event) {
        SessionManager.checkOut("Main Portal");
        activePage = "Dashboard";
        switchScene(event, "/com/example/findit/views/user/MainPortal.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent newRoot = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            AppWindow.setRoot(stage, newRoot, titleFor(fxmlPath));
        } catch (IOException e) {
            System.err.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private String titleFor(String fxmlPath) {
        int slash = fxmlPath.lastIndexOf('/');
        int dot = fxmlPath.lastIndexOf('.');
        String name = fxmlPath.substring(slash + 1, dot > slash ? dot : fxmlPath.length());
        return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }
}
