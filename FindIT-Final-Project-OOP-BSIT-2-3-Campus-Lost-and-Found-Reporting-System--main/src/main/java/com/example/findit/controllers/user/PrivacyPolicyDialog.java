package com.example.findit.controllers.user;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class PrivacyPolicyDialog {

    public static final String POLICY_TEXT = """
            PRIVACY POLICY

            Last Updated: 6/23/2026

            1. Introduction

            Welcome to findIT. We are committed to protecting your privacy and ensuring that your personal information is handled responsibly and securely.

            This Privacy Policy explains how we collect, use, store, disclose, and protect your personal information when you use our application. Our data processing practices are guided by the Data Privacy Act of 2012 (Republic Act No. 10173).

            By accessing or using the application, you acknowledge that you have read and understood this Privacy Policy.

            2. Information We Collect

            We may collect the following categories of information:

            Personal information
            - Full name for reporters of lost items; not required for reporters of found items
            - Contact information for reporters of lost items; not required for reporters of found items
            - Password for administrators

            User-generated Content
            - Name of item
            - Category of item
            - Location lost and found of items
            - Date lost and found of items
            - Description of lost and found items
            - Image of item (optional)

            3. Purpose of Collection and Processing

            We collect and process personal information for the following purposes:

            - To provide application services and features
            - To verify administrator identity
            - To automate the process of reporting, recording, and managing lost and found items within the campus
            - To improve application functionality and performance
            - To ensure system security and prevent unauthorized access
            - To improve the monitoring and tracking of item reports, claim requests, and item status through a centralized system
            - To ensure the accuracy and consistency of records by utilizing a database-driven information management system
            - To enhance the efficiency of lost and found operations by providing search, filtering, and claim verification functionalities
            - To assist administrators in validating ownership claims and updating item and claim records effectively
            - To provide students and staff with an organized platform for submitting, viewing, and managing lost and found reports

            We only collect information that is necessary and relevant for these purposes.

            4. Disclosure of Information

            We do not sell, rent, or trade personal information to third parties.

            5. Data Retention

            Personal information will be retained only for as long as necessary to fulfill the purposes described in this Privacy Policy, comply with legal obligations, resolve disputes, and enforce our agreements.

            Upon expiration of the retention period, personal information will be securely deleted, destroyed, or anonymized.

            6. Data Security

            We implement reasonable organizational, physical, and technical safeguards to protect personal information from:

            - Unauthorized access
            - Unauthorized disclosure
            - Loss or theft
            - Alteration or destruction
            - Misuse of information

            While we strive to protect your information, no method of transmission over the internet or electronic storage is completely secure.

            7. User Rights

            As a data subject under Philippine law, you have the right to:

            - Be informed about how your personal information is processed;
            - Access your personal information;
            - Correct inaccurate or incomplete information;
            - Object to processing when applicable;
            - Suspend, withdraw, or order the blocking of processing;
            - Request deletion or destruction of personal information when legally justified;
            - Obtain a copy of your personal information;

            Requests regarding these rights may be submitted through the contact information provided below.

            8. Changes to This Privacy Policy

            We reserve the right to update this Privacy Policy at any time. Any changes will be posted within the application and will become effective immediately upon publication unless otherwise stated.

            Users are encouraged to review this Privacy Policy periodically.

            9. Contact Information

            For questions, requests, or concerns regarding this Privacy Policy or our data processing practices, please contact:

            Project Manager
            findIT
            Email: lloydgabriel0619@gmail.com
            Phone: 09471029446

            10. Consent

            By using the application, you consent to the collection, processing, storage, and disclosure of your personal information in accordance with this Privacy Policy.
            """;

    public PrivacyPolicyDialog() {
    }

    public static void show(Window owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Privacy Policy");
        dialog.setHeaderText(null);
        if (owner != null) {
            dialog.initOwner(owner);
        }

        ButtonType understandButton = new ButtonType("I Understand", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(understandButton);
        dialogPane.setPrefWidth(720);
        dialogPane.setPrefHeight(620);
        dialogPane.setStyle("-fx-background-color: #FFFFFF;");

        Label title = new Label("Privacy Policy");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #FFCC00;");

        Label subtitle = new Label("Please read this before filling out the report form.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle("-fx-background-color: #800000; -fx-background-radius: 12 12 0 0;");

        Label policy = new Label(POLICY_TEXT);
        policy.setWrapText(true);
        policy.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-line-spacing: 2px;");
        policy.setPadding(new Insets(18));

        ScrollPane scrollPane = new ScrollPane(policy);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(430);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: #FFCC00; -fx-border-width: 0 2 2 2;");

        VBox content = new VBox(header, scrollPane);
        content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #800000; -fx-border-radius: 12; -fx-border-width: 2;");
        dialogPane.setContent(content);

        Node buttonNode = dialogPane.lookupButton(understandButton);
        if (buttonNode instanceof Button button) {
            button.setStyle("-fx-background-color: #FFCC00; -fx-text-fill: #4A1515; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 22; -fx-cursor: hand;");
        }

        dialog.showAndWait();
    }
}
