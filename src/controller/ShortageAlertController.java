package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import service.ShortageAlertService;
import service.ShortageAlertService.ShortageAlert;

public class ShortageAlertController extends BorderPane {

    private final ShortageAlertService service =
            new ShortageAlertService();

    private ArrayList<ShortageAlert> allRecords =
            new ArrayList<>();

    private final ObservableList<ShortageAlert> alertList =
            FXCollections.observableArrayList();

    private final TableView<ShortageAlert> alertTable =
            new TableView<>();

    private final TextField searchField = new TextField();

    private final ComboBox<String> riskFilter =
            new ComboBox<>();

    private final ComboBox<String> sortFilter =
            new ComboBox<>();

    private final Label totalLabel = new Label();
    private final Label criticalLabel = new Label();
    private final Label warningLabel = new Label();
    private final Label safeLabel = new Label();

    private final PieChart riskChart = new PieChart();

    public ShortageAlertController() {

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #F4F6FA;");

        createUI();
        createTable();
        refreshData();
    }

    // MAIN UI
    private void createUI() {

        Label heading =
                new Label("Attendance Shortage Alerts");

        heading.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
            "Monitor attendance risks and identify students requiring attention"
        );

        subtitle.setStyle(
            "-fx-text-fill: #64748B;" +
            "-fx-font-size: 13px;"
        );

        VBox header = new VBox(5, heading, subtitle);
        header.setPadding(new Insets(0, 0, 18, 0));

        // SUMMARY CARDS
        HBox cards = new HBox(12);

        cards.getChildren().addAll(
            createCard("Total Records", totalLabel, "#2563EB"),
            createCard("Critical Risk", criticalLabel, "#DC2626"),
            createCard("Warning Risk", warningLabel, "#D97706"),
            createCard("Safe Records", safeLabel, "#059669")
        );

        // SEARCH
        searchField.setPromptText(
            "Search roll number, student name or subject..."
        );

        searchField.setPrefWidth(280);

        searchField.setStyle(
            "-fx-padding: 10;" +
            "-fx-background-radius: 7;"
        );

        Button refreshButton =
                createButton("Refresh", "#475569");

        Button exportButton =
                createButton("Export CSV", "#059669");

        refreshButton.setOnAction(e -> {
            searchField.clear();
            riskFilter.setValue("All Risks");
            sortFilter.setValue("Lowest Attendance");
            refreshData();
        });

        exportButton.setOnAction(e -> exportCSV());

        // RISK FILTER
        riskFilter.setItems(
            FXCollections.observableArrayList(
                "All Risks",
                "CRITICAL",
                "WARNING",
                "SAFE"
            )
        );

        riskFilter.setValue("All Risks");
        riskFilter.setPrefWidth(140);

        // SORT FILTER
        sortFilter.setItems(
            FXCollections.observableArrayList(
                "Lowest Attendance",
                "Highest Risk Score",
                "Highest Attendance",
                "Roll Number"
            )
        );

        sortFilter.setValue("Lowest Attendance");
        sortFilter.setPrefWidth(180);

        riskFilter.setOnAction(e -> applyFilters());
        sortFilter.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );

        HBox searchBox = new HBox(
            10,
            searchField,
            riskFilter,
            sortFilter,
            refreshButton,
            exportButton
        );

        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(18, 0, 12, 0));

        // CHART
        riskChart.setTitle("Student Risk Distribution");
        riskChart.setLabelsVisible(true);
        riskChart.setLegendVisible(true);
        riskChart.setPrefHeight(220);

        VBox chartContainer = new VBox(10, riskChart);
        chartContainer.setPadding(new Insets(15));

        chartContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;"
        );

        Label tableHeading =
                new Label("Student Attendance Risk Details");

        tableHeading.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        VBox topContent = new VBox(
            header,
            cards,
            searchBox,
            chartContainer,
            tableHeading
        );

        VBox.setMargin(
            tableHeading,
            new Insets(15, 0, 8, 0)
        );

        setTop(topContent);
    }

    // SUMMARY CARD
    private VBox createCard(
            String title,
            Label value,
            String color) {

        Label heading = new Label(title);

        heading.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #64748B;" +
            "-fx-font-weight: bold;"
        );

        value.setStyle(
            "-fx-font-size: 25px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";"
        );

        VBox card = new VBox(10, heading, value);

        card.setPadding(new Insets(18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(150);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // CREATE TABLE
    private void createTable() {

        TableColumn<ShortageAlert, String> rollColumn =
                new TableColumn<>("Roll Number");

        rollColumn.setCellValueFactory(
            new PropertyValueFactory<>("rollNumber")
        );

        TableColumn<ShortageAlert, String> nameColumn =
                new TableColumn<>("Student Name");

        nameColumn.setCellValueFactory(
            new PropertyValueFactory<>("studentName")
        );

        TableColumn<ShortageAlert, String> subjectColumn =
                new TableColumn<>("Subject");

        subjectColumn.setCellValueFactory(
            new PropertyValueFactory<>("subjectName")
        );

        TableColumn<ShortageAlert, Integer> attendedColumn =
                new TableColumn<>("Attended");

        attendedColumn.setCellValueFactory(
            new PropertyValueFactory<>("attendedClasses")
        );

        TableColumn<ShortageAlert, Integer> totalColumn =
                new TableColumn<>("Total Classes");

        totalColumn.setCellValueFactory(
            new PropertyValueFactory<>("totalClasses")
        );

        TableColumn<ShortageAlert, Double> percentageColumn =
                new TableColumn<>("Attendance %");

        percentageColumn.setCellValueFactory(
            new PropertyValueFactory<>("percentage")
        );

        TableColumn<ShortageAlert, Integer> requiredColumn =
                new TableColumn<>("Classes Needed");

        requiredColumn.setCellValueFactory(
            new PropertyValueFactory<>("requiredClasses")
        );

        TableColumn<ShortageAlert, String> riskColumn =
                new TableColumn<>("Risk Level");

        riskColumn.setCellValueFactory(
            new PropertyValueFactory<>("riskLevel")
        );

        TableColumn<ShortageAlert, Double> scoreColumn =
                new TableColumn<>("Risk Score");

        scoreColumn.setCellValueFactory(
            new PropertyValueFactory<>("riskScore")
        );

        TableColumn<ShortageAlert, String> recommendationColumn =
                new TableColumn<>("Recommendation");

        recommendationColumn.setCellValueFactory(
            new PropertyValueFactory<>("recommendation")
        );

        alertTable.getColumns().addAll(
            rollColumn,
            nameColumn,
            subjectColumn,
            attendedColumn,
            totalColumn,
            percentageColumn,
            requiredColumn,
            riskColumn,
            scoreColumn,
            recommendationColumn
        );

        alertTable.setItems(alertList);

        // RISK-BASED ROW COLORS
        alertTable.setRowFactory(table ->
            new TableRow<ShortageAlert>() {

                @Override
                protected void updateItem(
                        ShortageAlert alert,
                        boolean empty) {

                    super.updateItem(alert, empty);

                    if (empty || alert == null) {
                        setStyle("");
                        return;
                    }

                    String risk = alert.getRiskLevel();

                    if ("CRITICAL".equalsIgnoreCase(risk)) {

                        setStyle(
                            "-fx-background-color: #FEE2E2;" +
                            "-fx-text-background-color: #991B1B;"
                        );

                    } else if ("WARNING".equalsIgnoreCase(risk)) {

                        setStyle(
                            "-fx-background-color: #FEF3C7;" +
                            "-fx-text-background-color: #92400E;"
                        );

                    } else {

                        setStyle(
                            "-fx-background-color: #D1FAE5;" +
                            "-fx-text-background-color: #065F46;"
                        );
                    }
                }
            }
        );

        alertTable.setColumnResizePolicy(
            TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        alertTable.setPlaceholder(
            new Label("No attendance risk records available")
        );

        alertTable.setPrefHeight(450);

        VBox container = new VBox(alertTable);
        container.setPadding(new Insets(10, 0, 0, 0));

        setCenter(container);
    }

    // REFRESH DATA
    private void refreshData() {

        allRecords = service.getAllRiskRecords();

        updateDashboard();
        updateChart();
        applyFilters();
    }

    // UPDATE SUMMARY
    private void updateDashboard() {

        int critical = 0;
        int warning = 0;
        int safe = 0;

        for (ShortageAlert alert : allRecords) {

            String risk = alert.getRiskLevel();

            if (risk.equalsIgnoreCase("CRITICAL")) {
                critical++;
            }
            else if (risk.equalsIgnoreCase("WARNING")) {
                warning++;
            }
            else {
                safe++;
            }
        }

        totalLabel.setText(
            String.valueOf(allRecords.size())
        );

        criticalLabel.setText(
            String.valueOf(critical)
        );

        warningLabel.setText(
            String.valueOf(warning)
        );

        safeLabel.setText(
            String.valueOf(safe)
        );
    }

    // UPDATE CHART
    private void updateChart() {

        int critical = 0;
        int warning = 0;
        int safe = 0;

        for (ShortageAlert alert : allRecords) {

            String risk = alert.getRiskLevel();

            if (risk.equalsIgnoreCase("CRITICAL")) {
                critical++;
            }
            else if (risk.equalsIgnoreCase("WARNING")) {
                warning++;
            }
            else {
                safe++;
            }
        }

        ObservableList<PieChart.Data> chartData =
                FXCollections.observableArrayList();

        if (critical > 0) {
            chartData.add(
                new PieChart.Data("Critical", critical)
            );
        }

        if (warning > 0) {
            chartData.add(
                new PieChart.Data("Warning", warning)
            );
        }

        if (safe > 0) {
            chartData.add(
                new PieChart.Data("Safe", safe)
            );
        }

        riskChart.setData(chartData);
    }

    // SEARCH + FILTER + SORT
    private void applyFilters() {

        String keyword = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedRisk = riskFilter.getValue();

        ArrayList<ShortageAlert> results =
                new ArrayList<>();

        for (ShortageAlert alert : allRecords) {

            boolean matchesSearch =
                    alert.getRollNumber()
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword)
                    ||
                    alert.getStudentName()
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword)
                    ||
                    alert.getSubjectName()
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword)
                    ||
                    alert.getRiskLevel()
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword);

            boolean matchesRisk =
                    selectedRisk == null
                    || selectedRisk.equals("All Risks")
                    || alert.getRiskLevel()
                        .equalsIgnoreCase(selectedRisk);

            if (matchesSearch && matchesRisk) {
                results.add(alert);
            }
        }

        String selectedSort = sortFilter.getValue();

        if ("Lowest Attendance".equals(selectedSort)) {

            results.sort(
                Comparator.comparingDouble(
                    ShortageAlert::getPercentage
                )
            );

        }
        else if ("Highest Risk Score".equals(selectedSort)) {

            // getRiskScore() RETURNS DOUBLE
            results.sort(
                Comparator.comparingDouble(
                    ShortageAlert::getRiskScore
                ).reversed()
            );

        }
        else if ("Highest Attendance".equals(selectedSort)) {

            results.sort(
                Comparator.comparingDouble(
                    ShortageAlert::getPercentage
                ).reversed()
            );

        }
        else if ("Roll Number".equals(selectedSort)) {

            results.sort(
                Comparator.comparing(
                    ShortageAlert::getRollNumber,
                    String.CASE_INSENSITIVE_ORDER
                )
            );
        }

        alertList.setAll(results);
    }

    // EXPORT FILTERED RESULTS
    private void exportCSV() {

        if (alertList.isEmpty()) {

            showMessage(
                Alert.AlertType.WARNING,
                "No Records",
                "There are no records to export."
            );

            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Export Attendance Risk Report");

        chooser.setInitialFileName(
            "ATTENDLY_Shortage_Report.csv"
        );

        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "CSV Files",
                "*.csv"
            )
        );

        File file = chooser.showSaveDialog(
            getScene() == null ? null : getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(file))) {

            writer.write(
                "Roll Number,Student Name,Subject,Attended Classes," +
                "Total Classes,Attendance Percentage,Classes Needed," +
                "Risk Level,Risk Score,Recommendation"
            );

            writer.newLine();

            for (ShortageAlert alert : alertList) {

                writer.write(
                    csv(alert.getRollNumber()) + "," +
                    csv(alert.getStudentName()) + "," +
                    csv(alert.getSubjectName()) + "," +
                    alert.getAttendedClasses() + "," +
                    alert.getTotalClasses() + "," +
                    alert.getPercentage() + "," +
                    alert.getRequiredClasses() + "," +
                    csv(alert.getRiskLevel()) + "," +
                    alert.getRiskScore() + "," +
                    csv(alert.getRecommendation())
                );

                writer.newLine();
            }

            showMessage(
                Alert.AlertType.INFORMATION,
                "Export Successful",
                "Filtered attendance risk report exported successfully."
            );

        } catch (Exception e) {

            showMessage(
                Alert.AlertType.ERROR,
                "Export Failed",
                "Could not export report: " + e.getMessage()
            );
        }
    }

    // CSV FORMATTING
    private String csv(String value) {

        if (value == null) {
            return "\"\"";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // BUTTON STYLE
    private Button createButton(
            String text,
            String color) {

        Button button = new Button(text);

        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 15 10 15;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;"
        );

        return button;
    }

    // ALERT MESSAGE
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
}