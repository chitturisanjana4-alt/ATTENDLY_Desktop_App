package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import service.UserService;
import model.UserAccount;

import java.util.function.Consumer;

public class LoginController extends BorderPane {

    private UserService userService = new UserService();

    private Consumer<UserAccount> onLoginSuccess;

    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;

    public LoginController(Consumer<UserAccount> onLoginSuccess) {

        this.onLoginSuccess = onLoginSuccess;

        setStyle("-fx-background-color: #f1f5f9;");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(35));

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 15;"
        );

        Label logo = new Label("ATTENDLY");

        logo.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
            "Smart Attendance Management System"
        );

        subtitle.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #64748b;"
        );

        Label heading = new Label("Welcome Back");

        heading.setStyle(
            "-fx-font-size: 21px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1e293b;"
        );

        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(42);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(42);

        Button loginButton = new Button("LOGIN");

        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(43);

        loginButton.setStyle(
            "-fx-background-color: #2563eb;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 7;"
        );

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setTextFill(Color.RED);

        loginButton.setOnAction(event -> authenticate());

        passwordField.setOnAction(event -> authenticate());

        Label footer = new Label(
            "Secure Role-Based Access"
        );

        footer.setStyle(
            "-fx-text-fill: #64748b;" +
            "-fx-font-size: 11px;"
        );

        card.getChildren().addAll(
            logo,
            subtitle,
            new Separator(),
            heading,
            usernameField,
            passwordField,
            loginButton,
            messageLabel,
            footer
        );

        StackPane container = new StackPane(card);
        container.setPadding(new Insets(25));

        setCenter(container);
    }

    private void authenticate() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {

            messageLabel.setText(
                "Please enter username and password."
            );

            return;
        }

        UserAccount user = userService.login(
            username,
            password
        );

        if (user == null) {

            messageLabel.setText(
                "Invalid username or password."
            );

            return;
        }

        messageLabel.setText("");

        if (onLoginSuccess != null) {
            onLoginSuccess.accept(user);
        }
    }
}