package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SystemHealthController extends BorderPane {

    private Label dataFolderStatus;
    private Label backupFolderStatus;
    private Label studentsStatus;
    private Label subjectsStatus;
    private Label attendanceStatus;
    private Label usersStatus;
    private Label auditStatus;
    private Label lastCheckedLabel;

    public SystemHealthController() {

        setPadding(new Insets(25));

        setStyle("-fx-background-color: #F4F6FA;");

        createUI();

        refreshHealth();
    }

    private void createUI() {

        VBox main = new VBox(20);

        Label heading = new Label("System Health Monitor");

        heading.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Label description = new Label(
                "Monitor the availability of ATTENDLY storage files and folders."
        );

        description.setStyle(
                "-fx-text-fill: #6B7280;" +
                "-fx-font-size: 13px;"
        );

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);

        dataFolderStatus = new Label();
        backupFolderStatus = new Label();
        studentsStatus = new Label();
        subjectsStatus = new Label();
        attendanceStatus = new Label();
        usersStatus = new Label();
        auditStatus = new Label();

        addHealthCard(
                grid, 0, 0,
                "DATA FOLDER",
                dataFolderStatus
        );

        addHealthCard(
                grid, 1, 0,
                "BACKUP FOLDER",
                backupFolderStatus
        );

        addHealthCard(
                grid, 0, 1,
                "STUDENT DATA",
                studentsStatus
        );

        addHealthCard(
                grid, 1, 1,
                "SUBJECT DATA",
                subjectsStatus
        );

        addHealthCard(
                grid, 0, 2,
                "ATTENDANCE DATA",
                attendanceStatus
        );

        addHealthCard(
                grid, 1, 2,
                "USER DATA",
                usersStatus
        );

        addHealthCard(
                grid, 0, 3,
                "AUDIT LOG DATA",
                auditStatus
        );

        Button refreshButton = new Button(
                "Refresh System Health"
        );

        refreshButton.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 20 10 20;"
        );

        refreshButton.setOnAction(e -> refreshHealth());

        lastCheckedLabel = new Label();

        lastCheckedLabel.setStyle(
                "-fx-text-fill: #6B7280;" +
                "-fx-font-size: 12px;"
        );

        VBox bottom = new VBox(10);

        bottom.setAlignment(Pos.CENTER_LEFT);

        bottom.getChildren().addAll(
                refreshButton,
                lastCheckedLabel
        );

        main.getChildren().addAll(
                heading,
                description,
                grid,
                bottom
        );

        setCenter(main);
    }

    private void addHealthCard(
            GridPane grid,
            int column,
            int row,
            String title,
            Label statusLabel
    ) {

        VBox card = new VBox(12);

        card.setPadding(new Insets(20));

        card.setPrefWidth(280);

        card.setMinHeight(105);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 10;"
        );

        Label titleLabel = new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #6B7280;"
        );

        statusLabel.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(
                titleLabel,
                statusLabel
        );

        grid.add(card, column, row);
    }

    private void refreshHealth() {

        checkFolder(
                "data",
                dataFolderStatus
        );

        checkFolder(
                "backups",
                backupFolderStatus
        );

        checkFile(
                "data/students.txt",
                studentsStatus
        );

        checkFile(
                "data/subjects.txt",
                subjectsStatus
        );

        checkFile(
                "data/attendance.txt",
                attendanceStatus
        );

        checkFile(
                "data/users.txt",
                usersStatus
        );

        checkFile(
                "data/audit_logs.txt",
                auditStatus
        );

        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"
                )
        );

        lastCheckedLabel.setText(
                "Last checked: " + time
        );
    }

    private void checkFolder(
            String path,
            Label statusLabel
    ) {

        File file = new File(path);

        if (!file.exists()) {

            setStatus(
                    statusLabel,
                    "NOT FOUND",
                    "#DC2626"
            );

        } else if (!file.isDirectory()) {

            setStatus(
                    statusLabel,
                    "INVALID FOLDER",
                    "#DC2626"
            );

        } else if (!file.canRead()) {

            setStatus(
                    statusLabel,
                    "NOT READABLE",
                    "#D97706"
            );

        } else {

            setStatus(
                    statusLabel,
                    "AVAILABLE",
                    "#059669"
            );
        }
    }

    private void checkFile(
            String path,
            Label statusLabel
    ) {

        File file = new File(path);

        if (!file.exists()) {

            setStatus(
                    statusLabel,
                    "NOT FOUND",
                    "#DC2626"
            );

        } else if (!file.isFile()) {

            setStatus(
                    statusLabel,
                    "INVALID FILE",
                    "#DC2626"
            );

        } else if (!file.canRead()) {

            setStatus(
                    statusLabel,
                    "NOT READABLE",
                    "#D97706"
            );

        } else {

            setStatus(
                    statusLabel,
                    "AVAILABLE",
                    "#059669"
            );
        }
    }

    private void setStatus(
            Label label,
            String text,
            String color
    ) {

        label.setText(text);

        label.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
        );
    }
}