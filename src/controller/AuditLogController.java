package controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import model.AuditLog;
import service.AuditLogService;

public class AuditLogController extends BorderPane {

    private final AuditLogService service =
            new AuditLogService();

    private final TableView<AuditLog> table =
            new TableView<>();

    private final ObservableList<AuditLog> records =
            FXCollections.observableArrayList();

    private final Label totalLabel =
            new Label("0");

    private final Label todayLabel =
            new Label("0");

    private final TextField searchField =
            new TextField();

    private final ComboBox<String> categoryFilter =
            new ComboBox<>();

    private final DatePicker dateFilter =
            new DatePicker();

    private final PieChart categoryChart =
            new PieChart();

    public AuditLogController() {

        setPadding(new Insets(20));

        createHeader();
        createSummaryCards();
        createFilters();
        createTable();
        createChart();

        refreshData();
    }

    private void createHeader() {

        Label heading =
                new Label("System Audit Logs");

        heading.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Monitor important activities performed in ATTENDLY."
                );

        subtitle.setStyle(
                "-fx-text-fill: #64748b;"
        );

        VBox header =
                new VBox(6, heading, subtitle);

        header.setPadding(
                new Insets(0, 0, 18, 0)
        );

        setTop(header);
    }

    private VBox createCard(
            String title,
            Label value) {

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: #64748b;" +
                "-fx-font-size: 12px;"
        );

        value.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        VBox card =
                new VBox(8, titleLabel, value);

        card.setPadding(new Insets(16));
        card.setPrefWidth(190);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 10;"
        );

        return card;
    }

    private void createSummaryCards() {

        HBox cards = new HBox(
                15,
                createCard("Total Activities", totalLabel),
                createCard("Today's Activities", todayLabel)
        );

        cards.setPadding(
                new Insets(0, 0, 18, 0)
        );

        VBox container =
                new VBox(cards);

        container.setPadding(
                new Insets(0, 0, 10, 0)
        );

        setCenter(container);
    }

    private void createFilters() {

        searchField.setPromptText(
                "Search action, category or details..."
        );

        searchField.setPrefWidth(300);

        categoryFilter.setPromptText(
                "Select category"
        );

        categoryFilter.setPrefWidth(150);

        categoryFilter.getItems().add("ALL");

        categoryFilter.setValue("ALL");

        dateFilter.setPromptText(
                "Filter by date"
        );

        Button clearDateButton =
                new Button("Clear Date");

        Button refreshButton =
                new Button("Refresh");

        Button exportButton =
                new Button("Export CSV");

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyFilters()
        );

        categoryFilter.setOnAction(
                event -> applyFilters()
        );

        dateFilter.setOnAction(
                event -> applyFilters()
        );

        clearDateButton.setOnAction(event -> {
            dateFilter.setValue(null);
            applyFilters();
        });

        refreshButton.setOnAction(event ->
                refreshData()
        );

        exportButton.setOnAction(event ->
                exportCSV()
        );

        HBox filters = new HBox(
                10,
                searchField,
                categoryFilter,
                dateFilter,
                clearDateButton,
                refreshButton,
                exportButton
        );

        filters.setPadding(
                new Insets(10, 0, 15, 0)
        );

        VBox center =
                (VBox) getCenter();

        center.getChildren().add(filters);
    }

    private void createTable() {

        TableColumn<AuditLog, String> actionColumn =
                new TableColumn<>("Action");

        actionColumn.setCellValueFactory(
                new PropertyValueFactory<>("action")
        );

        TableColumn<AuditLog, String> categoryColumn =
                new TableColumn<>("Category");

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        TableColumn<AuditLog, String> detailsColumn =
                new TableColumn<>("Details");

        detailsColumn.setCellValueFactory(
                new PropertyValueFactory<>("details")
        );

        TableColumn<AuditLog, String> dateColumn =
                new TableColumn<>("Date & Time");

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("formattedTimestamp")
        );

        table.getColumns().addAll(
                actionColumn,
                categoryColumn,
                detailsColumn,
                dateColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setItems(records);

        table.setPlaceholder(
                new Label("No audit records found.")
        );

        VBox center =
                (VBox) getCenter();

        center.getChildren().add(table);

        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private void createChart() {

        categoryChart.setTitle(
                "Activity by Category"
        );

        categoryChart.setLegendVisible(true);
        categoryChart.setLabelsVisible(true);
        categoryChart.setPrefHeight(230);

        VBox chartBox =
                new VBox(categoryChart);

        chartBox.setPadding(
                new Insets(15, 0, 0, 0)
        );

        setBottom(chartBox);
    }

    private void refreshData() {

        totalLabel.setText(
                String.valueOf(service.getTotalLogs())
        );

        todayLabel.setText(
                String.valueOf(service.getTodayLogCount())
        );

        String selectedCategory =
                categoryFilter.getValue();

        categoryFilter.getItems().setAll("ALL");

        categoryFilter.getItems().addAll(
                service.getAvailableCategories()
        );

        if (selectedCategory != null
                && categoryFilter.getItems()
                        .contains(selectedCategory)) {

            categoryFilter.setValue(selectedCategory);

        } else {

            categoryFilter.setValue("ALL");
        }

        applyFilters();
        updateChart();
    }

    private void applyFilters() {

        ArrayList<AuditLog> results =
                service.getAllLogs();

        String keyword =
                searchField.getText();

        String category =
                categoryFilter.getValue();

        LocalDate selectedDate =
                dateFilter.getValue();

        if (keyword != null && !keyword.isBlank()) {

            results = service.searchLogs(keyword);
        }

        if (category != null
                && !category.equals("ALL")) {

            String selected = category;

            results.removeIf(log ->
                    !log.getCategory()
                            .equalsIgnoreCase(selected)
            );
        }

        if (selectedDate != null) {

            results.removeIf(log ->
                    !log.getTimestamp()
                            .toLocalDate()
                            .equals(selectedDate)
            );
        }

        records.setAll(results);
    }

    private void updateChart() {

        categoryChart.getData().clear();

        for (String category :
                service.getAvailableCategories()) {

            long count =
                    service.getCategoryCount(category);

            categoryChart.getData().add(
                    new PieChart.Data(
                            category + " (" + count + ")",
                            count
                    )
            );
        }
    }

    private void exportCSV() {

        javafx.stage.FileChooser chooser =
                new javafx.stage.FileChooser();

        chooser.setTitle("Export Audit Logs");

        chooser.setInitialFileName(
                "ATTENDLY_Audit_Logs.csv"
        );

        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "CSV Files",
                        "*.csv"
                )
        );

        java.io.File file =
                chooser.showSaveDialog(
                        getScene().getWindow()
                );

        if (file == null) {
            return;
        }

        try (java.io.BufferedWriter writer =
                java.nio.file.Files.newBufferedWriter(
                        file.toPath(),
                        java.nio.charset.StandardCharsets.UTF_8)) {

            writer.write(
                    "Date Time,Category,Action,Details"
            );

            writer.newLine();

            for (AuditLog log : records) {

                writer.write(
                        csv(log.getFormattedTimestamp())
                        + ","
                        + csv(log.getCategory())
                        + ","
                        + csv(log.getAction())
                        + ","
                        + csv(log.getDetails())
                );

                writer.newLine();
            }

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Audit CSV exported successfully."
            ).showAndWait();

        } catch (Exception exception) {

            new Alert(
                    Alert.AlertType.ERROR,
                    "Export failed: " + exception.getMessage()
            ).showAndWait();
        }
    }

    private String csv(String value) {

        if (value == null) {
            return "\"\"";
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }
}