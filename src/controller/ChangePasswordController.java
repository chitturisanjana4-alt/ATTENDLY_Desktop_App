package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.UserAccount;
import service.UserService;

public class ChangePasswordController extends BorderPane {

    private UserService userService = new UserService();
    private UserAccount currentUser;

    private PasswordField oldPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    private Label messageLabel;

    public ChangePasswordController(UserAccount user) {

        currentUser = user;

        setStyle("-fx-background-color: #f1f5f9;");

        VBox card = new VBox(15);

        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(430);
        card.setPadding(new Insets(30));

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );

        Label heading = new Label("Change Password");

        heading.setStyle(
            "-fx-font-size: 25px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current password");

        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        Button changeButton = new Button("UPDATE PASSWORD");

        changeButton.setMaxWidth(Double.MAX_VALUE);
        changeButton.setPrefHeight(42);

        changeButton.setStyle(
            "-fx-background-color: #2563eb;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );

        messageLabel = new Label();
        messageLabel.setWrapText(true);

        changeButton.setOnAction(event -> changePassword());

        card.getChildren().addAll(
            heading,
            oldPasswordField,
            newPasswordField,
            confirmPasswordField,
            changeButton,
            messageLabel
        );

        StackPane container = new StackPane(card);
        container.setPadding(new Insets(25));

        setCenter(container);
    }

    private void changePassword() {

        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty()
                || newPassword.isEmpty()
                || confirmPassword.isEmpty()) {

            showMessage("Please fill all fields.", false);
            return;
        }

        if (newPassword.length() < 8) {

            showMessage(
                "New password must contain at least 8 characters.",
                false
            );

            return;
        }

        if (!newPassword.equals(confirmPassword)) {

            showMessage(
                "New password and confirmation do not match.",
                false
            );

            return;
        }

        boolean updated = userService.changePassword(
            currentUser.getUsername(),
            oldPassword,
            newPassword
        );

        if (updated) {

            currentUser.setPassword(newPassword);

            showMessage(
                "Password updated successfully.",
                true
            );

            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } else {

            showMessage(
                "Current password is incorrect.",
                false
            );
        }
    }

    private void showMessage(String message, boolean success) {

        messageLabel.setText(message);

        messageLabel.setStyle(
            success
                ? "-fx-text-fill: green;"
                : "-fx-text-fill: red;"
        );
    }
}