package controller;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import storage.BackupService;

public class BackupHistoryController extends BorderPane {

    private final BackupService backupService =
            new BackupService();

    private final TableView<BackupRecord> table =
            new TableView<>();

    private final ObservableList<BackupRecord> records =
            FXCollections.observableArrayList();

    private final Label countLabel =
            new Label("Total Backups: 0");

    private final Label statusLabel =
            new Label("Backup history loaded.");

    public BackupHistoryController() {

        setPadding(new Insets(20));

        createHeader();
        createTable();
        createActions();

        loadBackupHistory();
    }

    private void createHeader() {

        Label heading = new Label("Backup History");

        heading.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Label description = new Label(
                "View, inspect and restore your previous backups."
        );

        description.setStyle(
                "-fx-text-fill: #64748b;" +
                "-fx-font-size: 13px;"
        );

        VBox header = new VBox(6, heading, description);

        header.setPadding(new Insets(0, 0, 20, 0));

        setTop(header);
    }

    private void createTable() {

        TableColumn<BackupRecord, String> nameColumn =
                new TableColumn<>("Backup Folder");

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("backupName")
        );

        nameColumn.setPrefWidth(300);

        TableColumn<BackupRecord, String> dateColumn =
                new TableColumn<>("Backup Date");

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("backupDate")
        );

        dateColumn.setPrefWidth(250);

        TableColumn<BackupRecord, Integer> filesColumn =
                new TableColumn<>("Files Stored");

        filesColumn.setCellValueFactory(
                new PropertyValueFactory<>("fileCount")
        );

        filesColumn.setPrefWidth(150);

        table.getColumns().addAll(
                nameColumn,
                dateColumn,
                filesColumn
        );

        table.setItems(records);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setPlaceholder(
                new Label("No backups available.")
        );

        setCenter(table);
    }

    private void createActions() {

        Button refreshButton =
                new Button("Refresh History");

        Button restoreButton =
                new Button("Restore Selected Backup");

        refreshButton.setOnAction(event ->
                loadBackupHistory()
        );

        restoreButton.setOnAction(event ->
                restoreSelectedBackup()
        );

        refreshButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 16;"
        );

        restoreButton.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 16;"
        );

        HBox buttons = new HBox(
                12,
                refreshButton,
                restoreButton
        );

        VBox footer = new VBox(
                10,
                countLabel,
                buttons,
                statusLabel
        );

        footer.setPadding(new Insets(18, 0, 0, 0));

        setBottom(footer);
    }

    private void loadBackupHistory() {

        records.clear();

        List<Path> history =
                backupService.getBackupHistory();

        for (Path backupPath : history) {

            String folderName =
                    backupPath.getFileName().toString();

            String formattedDate =
                    formatBackupDate(folderName);

            int fileCount =
                    backupService.getFileCount(backupPath);

            records.add(
                    new BackupRecord(
                            folderName,
                            formattedDate,
                            fileCount,
                            backupPath
                    )
            );
        }

        countLabel.setText(
                "Total Backups: " + records.size()
        );

        statusLabel.setText(
                "Backup history refreshed successfully."
        );
    }

    private String formatBackupDate(String folderName) {

        try {

            String timestamp =
                    folderName.replace("backup_", "");

            DateTimeFormatter inputFormatter =
                    DateTimeFormatter.ofPattern(
                            "yyyyMMdd_HHmmss_SSS"
                    );

            LocalDateTime dateTime =
                    LocalDateTime.parse(
                            timestamp,
                            inputFormatter
                    );

            DateTimeFormatter outputFormatter =
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm:ss a"
                    );

            return dateTime.format(outputFormatter);

        } catch (DateTimeParseException exception) {

            return folderName;
        }
    }

    private void restoreSelectedBackup() {

        BackupRecord selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "No Backup Selected",
                    "Please select a backup from the table."
            );

            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Recovery");

        confirmation.setHeaderText(
                "Restore selected backup?"
        );

        confirmation.setContentText(
                "Current data files with matching names may be overwritten.\n\n"
                + "Selected backup: "
                + selected.getBackupName()
                + "\n\nCreate a fresh backup before restoring."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                String result =
                        backupService.restoreBackup(
                                selected.getBackupPath()
                        );

                statusLabel.setText(result);

                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Recovery Result",
                        result
                );
            }
        });
    }

    private void showMessage(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static class BackupRecord {

        private final String backupName;
        private final String backupDate;
        private final int fileCount;
        private final Path backupPath;

        public BackupRecord(
                String backupName,
                String backupDate,
                int fileCount,
                Path backupPath) {

            this.backupName = backupName;
            this.backupDate = backupDate;
            this.fileCount = fileCount;
            this.backupPath = backupPath;
        }

        public String getBackupName() {
            return backupName;
        }

        public String getBackupDate() {
            return backupDate;
        }

        public int getFileCount() {
            return fileCount;
        }

        public Path getBackupPath() {
            return backupPath;
        }
    }
}