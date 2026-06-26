package com.example.findit.controllers;

import com.example.findit.dao.UserDAO;
import com.example.findit.model.User;
import com.example.findit.util.InputValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegistrationController {

    @FXML private TextField idNumberField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField contactNumberField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleRegister() {
        String idNumber      = InputValidator.formatStudentNumber(idNumberField.getText());
        String fullName      = fullNameField.getText().trim();
        String password      = passwordField.getText();
        String confirmPass   = confirmPasswordField.getText();
        String contactNumber = contactNumberField.getText().trim();
        idNumberField.setText(idNumber);

        if (idNumber.isEmpty() || fullName.isEmpty() || password.isEmpty()
                || confirmPass.isEmpty() || contactNumber.isEmpty()) {
            showMessage("Please fill in all fields.", "error");
            return;
        }

        if (!password.equals(confirmPass)) {
            showMessage("Passwords do not match.", "error");
            return;
        }

        if (!InputValidator.isValidStudentNumber(idNumber)) {
            showMessage("Please use the correct student ID format, for example 2024-00000-SR-0.", "error");
            return;
        }

        if (!InputValidator.isValidNameText(fullName)) {
            showMessage("Please remove unsupported special characters from your name.", "error");
            return;
        }

        if (!InputValidator.isValidContact(contactNumber)) {
            showMessage("Please enter a valid 11-digit phone number or email address.", "error");
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters.", "error");
            return;
        }

        if (userDAO.isIdNumberTaken(idNumber)) {
            showMessage("This ID number is already registered.", "error");
            return;
        }

        User newUser = new User(idNumber, fullName, password, "Student", contactNumber);
        boolean success = userDAO.registerUser(newUser);

        if (success) {
            showMessage("Registration successful! You can now log in.", "success");
            handleClear();
        } else {
            showMessage("Registration failed. Please try again.", "error");
        }
    }

    @FXML
    public void handleClear() {
        idNumberField.clear();
        fullNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        contactNumberField.clear();
        messageLabel.setText("");
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if (type.equals("error")) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }
}

