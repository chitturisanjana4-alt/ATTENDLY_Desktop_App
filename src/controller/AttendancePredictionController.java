package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.AttendancePredictionService;
import service.AttendancePredictionService.AttendancePrediction;

public class AttendancePredictionController extends BorderPane {

    private final AttendancePredictionService service =
            new AttendancePredictionService();

    private final TableView<AttendancePrediction> table =
            new TableView<>();

    private final ObservableList<AttendancePrediction> data =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    private final ComboBox<String> riskFilter =
            new ComboBox<>();

    private final ComboBox<String> sortFilter =
            new ComboBox<>();

    private final Label totalLabel = new Label("0");
    private final Label criticalLabel = new Label("0");
    private final Label warningLabel = new Label("0");
    private final Label safeLabel = new Label("0");

    private BarChart<String, Number> chart;

    public AttendancePredictionController() {

        setPadding(new Insets(20));

        createHeader();
        createSummaryCards();
        createChart();
        createTable();
        createFilters();

        refreshData();
    }

    private void createHeader() {

        Label title = new Label("Attendance Prediction Analytics");
        title.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
                "Rule-based attendance forecasting and early warning"
        );

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));

        setTop(header);
    }

    private void createSummaryCards() {

        HBox cards = new HBox(15);

        cards.getChildren().addAll(
                createCard("Total Records", totalLabel, "#2563eb"),
                createCard("Critical Risk", criticalLabel, "#dc2626"),
                createCard("Warning Risk", warningLabel, "#d97706"),
                createCard("Safe", safeLabel, "#15803d")
        );

        cards.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(cards);
        container.setPadding(new Insets(0, 0, 20, 0));

        setLeft(null);

        if (getTop() instanceof VBox) {
            ((VBox) getTop()).getChildren().add(container);
        }
    }

    private VBox createCard(
            String heading,
            Label value,
            String color) {

        Label title = new Label(heading);

        title.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        value.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
        );

        VBox card = new VBox(10, title, value);

        card.setPadding(new Insets(18));
        card.setMinWidth(150);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 10;"
        );

        return card;
    }

    private void createChart() {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Risk Level");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Records");

        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        chart = new BarChart<>(xAxis, yAxis);

        chart.setTitle("Attendance Risk Distribution");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(250);

        VBox chartBox = new VBox(chart);
        chartBox.setPadding(new Insets(0, 0, 15, 0));

        setCenter(chartBox);
    }

    private void createFilters() {

        searchField.setPromptText("Search roll number or student...");
        searchField.setPrefWidth(250);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters()
        );

        riskFilter.getItems().addAll(
                "ALL",
                "CRITICAL",
                "WARNING",
                "SAFE"
        );

        riskFilter.setValue("ALL");

        riskFilter.setOnAction(event -> applyFilters());

        sortFilter.getItems().addAll(
                "Lowest Predicted Attendance",
                "Highest Predicted Attendance",
                "Highest Current Attendance",
                "Student Name"
        );

        sortFilter.setValue("Lowest Predicted Attendance");

        sortFilter.setOnAction(event -> applyFilters());

        HBox filters = new HBox(
                12,
                searchField,
                riskFilter,
                sortFilter
        );

        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 0, 10, 0));

        VBox container = new VBox(filters);

        if (getCenter() instanceof VBox) {
            ((VBox) getCenter()).getChildren().add(0, container);
        }
    }

    private void createTable() {

        TableColumn<AttendancePrediction, String> rollColumn =
                new TableColumn<>("Roll Number");

        rollColumn.setCellValueFactory(
                new PropertyValueFactory<>("rollNumber")
        );

        TableColumn<AttendancePrediction, String> nameColumn =
                new TableColumn<>("Student Name");

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentName")
        );

        TableColumn<AttendancePrediction, String> subjectColumn =
                new TableColumn<>("Subject");

        subjectColumn.setCellValueFactory(
                new PropertyValueFactory<>("subjectName")
        );

        TableColumn<AttendancePrediction, Integer> attendedColumn =
                new TableColumn<>("Attended");

        attendedColumn.setCellValueFactory(
                new PropertyValueFactory<>("attendedClasses")
        );

        TableColumn<AttendancePrediction, Integer> totalColumn =
                new TableColumn<>("Total Classes");

        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalClasses")
        );

        TableColumn<AttendancePrediction, Double> currentColumn =
                new TableColumn<>("Current %");

        currentColumn.setCellValueFactory(
                new PropertyValueFactory<>("currentPercentage")
        );

        currentColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<AttendancePrediction, Double>() {

                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f%%", value));
                        }
                    }
                }
        );

        TableColumn<AttendancePrediction, Double> predictedColumn =
                new TableColumn<>("Predicted %");

        predictedColumn.setCellValueFactory(
                new PropertyValueFactory<>("predictedPercentage")
        );

        predictedColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<AttendancePrediction, Double>() {

                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);

                        if (empty || value == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f%%", value));
                        }
                    }
                }
        );

        TableColumn<AttendancePrediction, String> riskColumn =
                new TableColumn<>("Risk Level");

        riskColumn.setCellValueFactory(
                new PropertyValueFactory<>("riskLevel")
        );

        TableColumn<AttendancePrediction, String> recommendationColumn =
                new TableColumn<>("Recommendation");

        recommendationColumn.setCellValueFactory(
                new PropertyValueFactory<>("recommendation")
        );

        table.getColumns().addAll(
                rollColumn,
                nameColumn,
                subjectColumn,
                attendedColumn,
                totalColumn,
                currentColumn,
                predictedColumn,
                riskColumn,
                recommendationColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setItems(data);

        VBox tableBox = new VBox(table);
        tableBox.setPadding(new Insets(10, 0, 0, 0));

        if (getCenter() instanceof VBox) {
            ((VBox) getCenter()).getChildren().add(tableBox);
        }
    }

    private void refreshData() {

        ArrayList<AttendancePrediction> predictions =
                service.getAllPredictions();

        totalLabel.setText(
                String.valueOf(predictions.size())
        );

        criticalLabel.setText(
                String.valueOf(service.getCriticalCount())
        );

        warningLabel.setText(
                String.valueOf(service.getWarningCount())
        );

        int safeCount = 0;

        for (AttendancePrediction prediction : predictions) {

            if ("SAFE".equals(prediction.getRiskLevel())) {
                safeCount++;
            }
        }

        safeLabel.setText(String.valueOf(safeCount));

        updateChart(predictions);
        applyFilters();
    }

    private void updateChart(
            ArrayList<AttendancePrediction> predictions) {

        int critical = 0;
        int warning = 0;
        int safe = 0;

        for (AttendancePrediction prediction : predictions) {

            switch (prediction.getRiskLevel()) {

                case "CRITICAL":
                    critical++;
                    break;

                case "WARNING":
                    warning++;
                    break;

                case "SAFE":
                    safe++;
                    break;
            }
        }

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        series.getData().add(
                new XYChart.Data<>("Critical", critical)
        );

        series.getData().add(
                new XYChart.Data<>("Warning", warning)
        );

        series.getData().add(
                new XYChart.Data<>("Safe", safe)
        );

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void applyFilters() {

        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String selectedRisk = riskFilter.getValue();

        String selectedSort = sortFilter.getValue();

        ArrayList<AttendancePrediction> predictions =
                service.getAllPredictions();

        predictions = predictions.stream()
                .filter(p ->
                        p.getRollNumber().toLowerCase().contains(keyword)
                        || p.getStudentName().toLowerCase().contains(keyword)
                        || p.getSubjectName().toLowerCase().contains(keyword)
                )
                .filter(p ->
                        selectedRisk == null
                        || selectedRisk.equals("ALL")
                        || p.getRiskLevel().equals(selectedRisk)
                )
                .collect(Collectors.toCollection(ArrayList::new));

        if (selectedSort != null) {

            switch (selectedSort) {

                case "Lowest Predicted Attendance":

                    predictions.sort(
                            Comparator.comparingDouble(
                                    AttendancePrediction::getPredictedPercentage
                            )
                    );
                    break;

                case "Highest Predicted Attendance":

                    predictions.sort(
                            Comparator.comparingDouble(
                                    AttendancePrediction::getPredictedPercentage
                            ).reversed()
                    );
                    break;

                case "Highest Current Attendance":

                    predictions.sort(
                            Comparator.comparingDouble(
                                    AttendancePrediction::getCurrentPercentage
                            ).reversed()
                    );
                    break;

                case "Student Name":

                    predictions.sort(
                            Comparator.comparing(
                                    AttendancePrediction::getStudentName,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                    );
                    break;
            }
        }

        data.setAll(predictions);
    }

    public void refreshPredictions() {
        refreshData();
    }
}