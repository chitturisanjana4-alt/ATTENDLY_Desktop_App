package controller;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import model.UserAccount;
import service.UserService;

public class UserManagementController extends BorderPane {

    private UserService userService;

    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private ComboBox<String> editRoleComboBox;

    private TableView<UserAccount> userTable;

    private ObservableList<UserAccount> userList;

    public UserManagementController() {

        userService = new UserService();

        userList = FXCollections.observableArrayList();

        createHeader();
        createForm();
        createTable();

        refreshUsers();
    }

    // HEADER
    private void createHeader() {

        VBox header = new VBox(5);

        header.setPadding(new Insets(20));

        Label title = new Label("User Management");

        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Create, manage and control application accounts"
        );

        subtitle.setStyle(
                "-fx-text-fill: #64748b;" +
                "-fx-font-size: 13px;"
        );

        header.getChildren().addAll(title, subtitle);

        setTop(header);
    }

    // CREATE USER FORM
    private void createForm() {

        VBox form = new VBox(12);

        form.setPadding(new Insets(20));

        form.setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-background-radius: 10;"
        );

        Label heading = new Label("Create New Account");

        heading.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        usernameField = new TextField();

        usernameField.setPromptText("Enter username");

        passwordField = new PasswordField();

        passwordField.setPromptText(
                "Enter password (minimum 8 characters)"
        );

        roleComboBox = new ComboBox<>();

        roleComboBox.getItems().addAll(
                "FACULTY",
                "STUDENT"
        );

        roleComboBox.setPromptText("Select role");

        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        Button addButton = new Button("Create Account");

        addButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        addButton.setOnAction(e -> addUser());

        form.getChildren().addAll(
                heading,
                usernameField,
                passwordField,
                roleComboBox,
                addButton
        );

        setCenter(form);
    }

    // USER TABLE
    private void createTable() {

        VBox tableSection = new VBox(12);

        tableSection.setPadding(new Insets(20));

        Label heading = new Label("Registered Accounts");

        heading.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        userTable = new TableView<>();

        userTable.setItems(userList);

        TableColumn<UserAccount, String> usernameColumn =
                new TableColumn<>("Username");

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        usernameColumn.setPrefWidth(200);

        TableColumn<UserAccount, String> roleColumn =
                new TableColumn<>("Role");

        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        roleColumn.setPrefWidth(150);

        userTable.getColumns().addAll(
                usernameColumn,
                roleColumn
        );

        editRoleComboBox = new ComboBox<>();

        editRoleComboBox.getItems().addAll(
                "ADMIN",
                "FACULTY",
                "STUDENT"
        );

        editRoleComboBox.setPromptText("New role");

        Button updateButton = new Button("Update Role");

        updateButton.setOnAction(e -> updateRole());

        Button deleteButton = new Button("Delete User");

        deleteButton.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;"
        );

        deleteButton.setOnAction(e -> deleteUser());

        Button refreshButton = new Button("Refresh");

        refreshButton.setOnAction(e -> refreshUsers());

        HBox actions = new HBox(10);

        actions.setAlignment(Pos.CENTER_LEFT);

        actions.getChildren().addAll(
                editRoleComboBox,
                updateButton,
                deleteButton,
                refreshButton
        );

        tableSection.getChildren().addAll(
                heading,
                userTable,
                actions
        );

        setBottom(tableSection);

    }

    // ADD USER
    private void addUser() {

        String username = usernameField.getText().trim();

        String password = passwordField.getText();

        String role = roleComboBox.getValue();

        if (username.isEmpty()
                || password.isEmpty()
                || role == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Please enter all account details."
            );

            return;
        }

        if (password.length() < 8) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Password must contain at least 8 characters."
            );

            return;
        }

        UserAccount user = new UserAccount(
                username,
                password,
                role
        );

        boolean success = userService.addUser(user);

        if (success) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Account created successfully."
            );

            usernameField.clear();
            passwordField.clear();
            roleComboBox.setValue(null);

            refreshUsers();

        } else {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Account creation failed. Username may already exist."
            );
        }
    }

    // UPDATE ROLE
    private void updateRole() {

        UserAccount selectedUser =
                userTable.getSelectionModel().getSelectedItem();

        String newRole = editRoleComboBox.getValue();

        if (selectedUser == null || newRole == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Select a user and a new role."
            );

            return;
        }

        boolean success = userService.updateUserRole(
                selectedUser.getUsername(),
                newRole
        );

        if (success) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "User role updated successfully."
            );

            editRoleComboBox.setValue(null);

            refreshUsers();

        } else {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Cannot update this account. The main admin account is protected."
            );
        }
    }

    // DELETE USER
    private void deleteUser() {

        UserAccount selectedUser =
                userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Please select a user to delete."
            );

            return;
        }

        if (selectedUser.getUsername().equalsIgnoreCase("admin")) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "The main admin account cannot be deleted."
            );

            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete Account");

        confirmation.setHeaderText("Delete selected user?");

        confirmation.setContentText(
                "Username: " + selectedUser.getUsername()
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == javafx.scene.control.ButtonType.OK) {

                boolean success = userService.deleteUser(
                        selectedUser.getUsername()
                );

                if (success) {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "User deleted successfully."
                    );

                    refreshUsers();

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Unable to delete user."
                    );
                }
            }
        });
    }

    // REFRESH TABLE
    private void refreshUsers() {

        ArrayList<UserAccount> users =
                userService.getAllUsers();

        userList.setAll(users);
    }

    // ALERT
    private void showAlert(
            Alert.AlertType type,
            String message) {

        Alert alert = new Alert(type);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}