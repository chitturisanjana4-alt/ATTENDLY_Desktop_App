package app;

import controller.*;
import model.UserAccount;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;

    private BorderPane root;
    private VBox sidebar;
    private BorderPane contentArea;

    private Label pageTitle;
    private Label userInfoLabel;

    private Button dashboardButton;
    private Button studentsButton;
    private Button subjectsButton;
    private Button attendanceButton;
    private Button calendarButton;
    private Button shortageButton;
    private Button reportsButton;
    private Button predictionButton;
    private Button userManagementButton;
    private Button backupButton;
    private Button backupHistoryButton;
    private Button auditLogsButton;
    private Button systemHealthButton;

    private UserAccount currentUser;

    private BackupController backupController;

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        primaryStage.setTitle("ATTENDLY - Smart Attendance System");

        showLoginScreen();

        primaryStage.show();
    }

    // LOGIN SCREEN
    private void showLoginScreen() {

        LoginController loginController =
                new LoginController(this::handleLoginSuccess);

        Scene scene = new Scene(
                loginController,
                1100,
                700
        );

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // LOGIN SUCCESS
    private void handleLoginSuccess(UserAccount user) {

        currentUser = user;

        showMainLayout();
    }

    // MAIN APPLICATION
    private void showMainLayout() {

        root = new BorderPane();

        root.setStyle("-fx-background-color: #F4F6FA;");

        createHeader();

        createSidebar();

        contentArea = new BorderPane();

        root.setCenter(contentArea);

        Scene scene = new Scene(
                root,
                1400,
                850
        );

        primaryStage.setScene(scene);

        primaryStage.centerOnScreen();

        if (isStudent()) {

            showStudentWelcome();

        } else {

            openScreen(
                    new DashboardController(isAdmin()),
                    "Dashboard"
            );
        }
    }

    // HEADER
    private void createHeader() {

        HBox header = new HBox(20);

        header.setPadding(new Insets(15, 25, 15, 25));

        header.setAlignment(Pos.CENTER_LEFT);

        header.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #E5E7EB;" +
                "-fx-border-width: 0 0 1 0;"
        );

        Label appName = new Label("ATTENDLY");

        appName.setStyle(
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2563EB;"
        );

        pageTitle = new Label("Dashboard");

        pageTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        HBox spacer = new HBox();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        userInfoLabel = new Label(
                currentUser.getUsername() +
                "  |  " +
                currentUser.getRole()
        );

        userInfoLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #374151;"
        );

        Button changePasswordButton =
                new Button("Change Password");

        changePasswordButton.setOnAction(e -> {

            ChangePasswordController controller =
                    new ChangePasswordController(currentUser);

            openScreen(
                    controller,
                    "Change Password"
            );
        });

        Button logoutButton =
                new Button("Logout");

        logoutButton.setStyle(
                "-fx-background-color: #DC2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        logoutButton.setOnAction(e -> logout());

        header.getChildren().addAll(
                appName,
                pageTitle,
                spacer,
                userInfoLabel,
                changePasswordButton,
                logoutButton
        );

        root.setTop(header);
    }

    // SIDEBAR
    private void createSidebar() {

        sidebar = new VBox(10);

        sidebar.setPadding(new Insets(20));

        sidebar.setPrefWidth(230);

        sidebar.setStyle(
                "-fx-background-color: #111827;"
        );

        dashboardButton =
                createNavigationButton("Dashboard");

        studentsButton =
                createNavigationButton("Students");

        subjectsButton =
                createNavigationButton("Subjects");

        attendanceButton =
                createNavigationButton("Attendance");

        calendarButton =
                createNavigationButton("Attendance Calendar");

        shortageButton =
                createNavigationButton("Shortage Alerts");

        reportsButton =
                createNavigationButton("Reports");

        predictionButton =
                createNavigationButton("Attendance Prediction");

        userManagementButton =
                createNavigationButton("User Management");

        backupButton =
                createNavigationButton("Backup & Recovery");

        backupHistoryButton =
                createNavigationButton("Backup History");

        auditLogsButton =
                createNavigationButton("Audit Logs");

        systemHealthButton =
                createNavigationButton("System Health");

        dashboardButton.setOnAction(e ->
                openScreen(
                        new DashboardController(isAdmin()),
                        "Dashboard"
                )
        );

        studentsButton.setOnAction(e ->
                openScreen(
                        new StudentController(),
                        "Students"
                )
        );

        subjectsButton.setOnAction(e ->
                openScreen(
                        new SubjectController(),
                        "Subjects"
                )
        );

        attendanceButton.setOnAction(e ->
                openScreen(
                        new AttendanceController(),
                        "Attendance"
                )
        );

        calendarButton.setOnAction(e ->
                openScreen(
                        new AttendanceCalendarController(),
                        "Attendance Calendar"
                )
        );

        shortageButton.setOnAction(e ->
                openScreen(
                        new ShortageAlertController(),
                        "Shortage Alerts"
                )
        );

        reportsButton.setOnAction(e ->
                openScreen(
                        new ReportsController(),
                        "Reports"
                )
        );

        predictionButton.setOnAction(e ->
                openScreen(
                        new AttendancePredictionController(),
                        "Attendance Prediction"
                )
        );

        userManagementButton.setOnAction(e ->
                openScreen(
                        new UserManagementController(),
                        "User Management"
                )
        );

        backupButton.setOnAction(e ->
                openScreen(
                        backupController,
                        "Backup & Recovery"
                )
        );

        backupHistoryButton.setOnAction(e ->
                openScreen(
                        new BackupHistoryController(),
                        "Backup History"
                )
        );

        auditLogsButton.setOnAction(e ->
                openScreen(
                        new AuditLogController(),
                        "Audit Logs"
                )
        );

        systemHealthButton.setOnAction(e ->
                openScreen(
                        new SystemHealthController(),
                        "System Health"
                )
        );

        Label menuLabel = new Label("MAIN MENU");

        menuLabel.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        sidebar.getChildren().add(menuLabel);

        if (isAdmin()) {

            backupController = new BackupController();

            sidebar.getChildren().addAll(
                    dashboardButton,
                    studentsButton,
                    subjectsButton,
                    attendanceButton,
                    calendarButton,
                    shortageButton,
                    reportsButton,
                    predictionButton,
                    userManagementButton,
                    backupButton,
                    backupHistoryButton,
                    auditLogsButton,
                    systemHealthButton
            );

        } else if (isFaculty()) {

            sidebar.getChildren().addAll(
                    dashboardButton,
                    studentsButton,
                    subjectsButton,
                    attendanceButton,
                    calendarButton,
                    shortageButton,
                    reportsButton,
                    predictionButton
            );
        }

        root.setLeft(sidebar);
    }

    // NAVIGATION BUTTON
    private Button createNavigationButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.setAlignment(Pos.CENTER_LEFT);

        button.setPadding(
                new Insets(12, 15, 12, 15)
        );

        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #374151;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
                )
        );

        return button;
    }

    // OPEN SCREEN
    private void openScreen(
            javafx.scene.Node screen,
            String title
    ) {

        if (contentArea == null || screen == null) {
            return;
        }

        pageTitle.setText(title);

        contentArea.setCenter(screen);
    }

    // STUDENT WELCOME SCREEN
    private void showStudentWelcome() {

        VBox welcome = new VBox(20);

        welcome.setAlignment(Pos.CENTER);

        welcome.setPadding(new Insets(30));

        Label title = new Label(
                "Welcome to ATTENDLY"
        );

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2563EB;"
        );

        Label username = new Label(
                "Hello, " + currentUser.getUsername()
        );

        username.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label message = new Label(
                "Your student account is active."
        );

        message.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #6B7280;"
        );

        welcome.getChildren().addAll(
                title,
                username,
                message
        );

        openScreen(
                welcome,
                "Student Home"
        );
    }

    // ROLE CHECKS
    private boolean isAdmin() {

        return currentUser != null &&
                "ADMIN".equalsIgnoreCase(
                        currentUser.getRole()
                );
    }

    private boolean isFaculty() {

        return currentUser != null &&
                "FACULTY".equalsIgnoreCase(
                        currentUser.getRole()
                );
    }

    private boolean isStudent() {

        return currentUser != null &&
                "STUDENT".equalsIgnoreCase(
                        currentUser.getRole()
                );
    }

    // LOGOUT
    private void logout() {

        shutdownBackup();

        currentUser = null;

        backupController = null;

        showLoginScreen();
    }

    // BACKUP SCHEDULER SHUTDOWN
    private void shutdownBackup() {

        if (backupController != null) {

            backupController.shutdownScheduler();
        }
    }

    @Override
    public void stop() {

        shutdownBackup();
    }

    public static void main(String[] args) {

        launch(args);
    }
}