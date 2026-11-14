package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import storage.BackupService;
import storage.AutoBackupScheduler;

public class BackupController extends BorderPane {

    private final BackupService backupService =
            new BackupService();

    private final AutoBackupScheduler scheduler =
            new AutoBackupScheduler();

    private final Label backupCountLabel =
            new Label("0");

    private final Label statusLabel =
            new Label("Automatic backup is stopped.");

    private final Label schedulerLabel =
            new Label("STOPPED");

    private final ComboBox<Integer> intervalBox =
            new ComboBox<>();

    public BackupController() {

        setPadding(new Insets(25));

        createScreen();
        refreshBackupCount();
        updateSchedulerStatus();
    }

    private void createScreen() {

        Label title = new Label("Backup & Recovery");

        title.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
                "Manage manual and automatic data backups"
        );

        subtitle.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox header = new VBox(8, title, subtitle);

        header.setPadding(
                new Insets(0, 0, 25, 0)
        );

        Label countTitle = new Label("Available Backups");

        countTitle.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #64748b;"
        );

        backupCountLabel.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2563eb;"
        );

        VBox countCard = new VBox(
                10,
                countTitle,
                backupCountLabel
        );

        countCard.setPadding(new Insets(20));
        countCard.setMaxWidth(280);

        countCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 12;"
        );

        // Manual backup

        Button manualButton =
                createButton("Create Backup", "#2563eb");

        manualButton.setOnAction(event -> createBackup());

        // Automatic backup interval

        intervalBox.getItems().addAll(
                1, 5, 15, 30, 60, 1440
        );

        intervalBox.setValue(60);
        intervalBox.setPrefWidth(180);

        intervalBox.setPromptText("Select interval");

        Label intervalInfo = new Label(
                "Interval is measured in minutes."
        );

        intervalInfo.setStyle(
                "-fx-text-fill: #64748b;"
        );

        // Start automatic backup

        Button startButton =
                createButton("Start Auto Backup", "#15803d");

        startButton.setOnAction(event -> startScheduler());

        // Stop automatic backup

        Button stopButton =
                createButton("Stop Auto Backup", "#dc2626");

        stopButton.setOnAction(event -> stopScheduler());

        // Restore backup

        Button restoreButton =
                createButton("Restore Latest Backup", "#d97706");

        restoreButton.setOnAction(event -> restoreBackup());

        // Refresh

        Button refreshButton =
                createButton("Refresh", "#475569");

        refreshButton.setOnAction(event -> {
            refreshBackupCount();
            updateSchedulerStatus();
        });

        VBox actions = new VBox(
                15,
                new Label("Manual Backup"),
                manualButton,
                new Label("Automatic Backup Interval"),
                intervalBox,
                intervalInfo,
                new HBox(10, startButton, stopButton),
                new Label("Recovery"),
                restoreButton,
                refreshButton
        );

        actions.setPadding(new Insets(10, 0, 10, 0));

        Label statusTitle =
                new Label("Automatic Backup Status");

        statusTitle.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;"
        );

        schedulerLabel.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #dc2626;"
        );

        statusLabel.setWrapText(true);

        VBox statusCard = new VBox(
                12,
                statusTitle,
                schedulerLabel,
                statusLabel
        );

        statusCard.setPadding(new Insets(20));
        statusCard.setMaxWidth(600);

        statusCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 10;"
        );

        VBox content = new VBox(
                22,
                countCard,
                actions,
                statusCard
        );

        setTop(header);
        setCenter(content);
    }

    private Button createButton(
            String text,
            String color) {

        Button button = new Button(text);

        button.setPrefWidth(210);
        button.setPrefHeight(40);

        button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 7;"
        );

        return button;
    }

    private void createBackup() {

        String result = backupService.createBackup();

        statusLabel.setText(result);

        refreshBackupCount();

        showMessage(
                "Backup Result",
                result,
                Alert.AlertType.INFORMATION
        );
    }

    private void startScheduler() {

        Integer interval = intervalBox.getValue();

        if (interval == null) {

            showMessage(
                    "Select Interval",
                    "Please select an automatic backup interval.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        String result =
                scheduler.startAutoBackup(interval);

        statusLabel.setText(result);

        updateSchedulerStatus();

    }

    private void stopScheduler() {

        String result = scheduler.stopAutoBackup();

        statusLabel.setText(result);

        updateSchedulerStatus();
    }

    private void restoreBackup() {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Recovery");
        confirmation.setHeaderText("Restore Latest Backup?");

        confirmation.setContentText(
                "Matching files in the data folder will be overwritten. "
                + "Create a fresh backup of current data before continuing."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                String result =
                        backupService.restoreLatestBackup();

                statusLabel.setText(result);

                refreshBackupCount();

                showMessage(
                        "Recovery Result",
                        result,
                        Alert.AlertType.INFORMATION
                );
            }
        });
    }

    private void refreshBackupCount() {

        backupCountLabel.setText(
                String.valueOf(
                        backupService.getBackupCount()
                )
        );
    }

    private void updateSchedulerStatus() {

        if (scheduler.isRunning()) {

            schedulerLabel.setText("RUNNING");

            schedulerLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #15803d;"
            );

        } else {

            schedulerLabel.setText("STOPPED");

            schedulerLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #dc2626;"
            );
        }
    }

    private void showMessage(
            String title,
            String message,
            Alert.AlertType type) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public void shutdownScheduler() {
        scheduler.shutdown();
    }
}