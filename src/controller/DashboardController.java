package controller;

import service.StudentService;
import service.SubjectService;
import service.AttendanceService;
import service.ShortageAlertService;
import service.ShortageAlertService.ShortageAlert;
import service.UserService;

import model.Attendance;
import model.Subject;

import javafx.collections.FXCollections;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;

public class DashboardController extends BorderPane {

    private StudentService studentService;
    private SubjectService subjectService;
    private AttendanceService attendanceService;
    private ShortageAlertService alertService;
    private UserService userService;

    private Label studentValue;
    private Label subjectValue;
    private Label attendanceValue;
    private Label percentageValue;
    private Label shortageValue;

    private Label criticalValue;
    private Label warningValue;
    private Label safeValue;

    private Label totalUsersValue;
    private Label adminUsersValue;
    private Label facultyUsersValue;
    private Label studentUsersValue;

    private PieChart attendanceChart;
    private BarChart<String, Number> subjectChart;

    private boolean adminMode;

    public DashboardController() {
        this(false);
    }

    public DashboardController(boolean adminMode) {

        this.adminMode = adminMode;

        studentService = new StudentService();
        subjectService = new SubjectService();
        attendanceService = new AttendanceService();
        alertService = new ShortageAlertService();
        userService = new UserService();

        setPadding(new Insets(20));

        setStyle("-fx-background-color: #F1F5F9;");

        createDashboard();

        refreshDashboard();
    }

    // MAIN DASHBOARD
    private void createDashboard() {

        VBox content = new VBox(20);

        content.setPadding(new Insets(5));

        createHeader(content);

        createKpiCards(content);

        createRiskCards(content);

        if (adminMode) {
            createUserStatistics(content);
        }

        createCharts(content);

        ScrollPane scrollPane = new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background: #F1F5F9;" +
                "-fx-background-color: transparent;"
        );

        setCenter(scrollPane);
    }

    // HEADER
    private void createHeader(VBox root) {

        Label title = new Label(
                "Attendance Overview"
        );

        title.setFont(Font.font("Arial", 27));

        title.setTextFill(Color.web("#172554"));

        Label subtitle = new Label(
                "Live analytics and institutional attendance insights"
        );

        subtitle.setTextFill(Color.web("#64748B"));

        Button refreshButton = new Button("Refresh Dashboard");

        refreshButton.setOnAction(e -> refreshDashboard());

        refreshButton.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 10 16;" +
                "-fx-background-radius: 7;"
        );

        BorderPane header = new BorderPane();

        header.setLeft(new VBox(7, title, subtitle));

        header.setRight(refreshButton);

        root.getChildren().add(header);
    }

    // KPI CARDS
    private void createKpiCards(VBox root) {

        studentValue = new Label("0");
        subjectValue = new Label("0");
        attendanceValue = new Label("0");
        percentageValue = new Label("0%");
        shortageValue = new Label("0");

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);

        grid.add(createCard(
                "TOTAL STUDENTS",
                studentValue,
                "#2563EB"
        ), 0, 0);

        grid.add(createCard(
                "TOTAL SUBJECTS",
                subjectValue,
                "#7C3AED"
        ), 1, 0);

        grid.add(createCard(
                "ATTENDANCE RECORDS",
                attendanceValue,
                "#0891B2"
        ), 2, 0);

        grid.add(createCard(
                "OVERALL ATTENDANCE",
                percentageValue,
                "#15803D"
        ), 0, 1);

        grid.add(createCard(
                "SHORTAGE RECORDS",
                shortageValue,
                "#DC2626"
        ), 1, 1);

        root.getChildren().add(grid);
    }

    // RISK SUMMARY CARDS
    private void createRiskCards(VBox root) {

        Label heading = new Label("Student Risk Overview");

        heading.setFont(Font.font("Arial", 19));

        heading.setTextFill(Color.web("#172554"));

        criticalValue = new Label("0");
        warningValue = new Label("0");
        safeValue = new Label("0");

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);

        grid.add(createCard(
                "CRITICAL RISK",
                criticalValue,
                "#DC2626"
        ), 0, 0);

        grid.add(createCard(
                "WARNING RISK",
                warningValue,
                "#D97706"
        ), 1, 0);

        grid.add(createCard(
                "SAFE STUDENTS",
                safeValue,
                "#15803D"
        ), 2, 0);

        root.getChildren().addAll(
                heading,
                grid
        );
    }

    // ADMIN USER STATISTICS
    private void createUserStatistics(VBox root) {

        Label heading = new Label(
                "User Account Overview"
        );

        heading.setFont(Font.font("Arial", 19));

        heading.setTextFill(Color.web("#172554"));

        totalUsersValue = new Label("0");
        adminUsersValue = new Label("0");
        facultyUsersValue = new Label("0");
        studentUsersValue = new Label("0");

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);

        grid.add(createCard(
                "TOTAL USERS",
                totalUsersValue,
                "#2563EB"
        ), 0, 0);

        grid.add(createCard(
                "ADMIN ACCOUNTS",
                adminUsersValue,
                "#7C3AED"
        ), 1, 0);

        grid.add(createCard(
                "FACULTY ACCOUNTS",
                facultyUsersValue,
                "#0891B2"
        ), 2, 0);

        grid.add(createCard(
                "STUDENT ACCOUNTS",
                studentUsersValue,
                "#15803D"
        ), 3, 0);

        root.getChildren().addAll(
                heading,
                grid
        );
    }

    // CARD DESIGN
    private VBox createCard(
            String heading,
            Label value,
            String color) {

        Label title = new Label(heading);

        title.setFont(Font.font("Arial", 12));

        title.setTextFill(Color.web("#64748B"));

        value.setFont(Font.font("Arial", 27));

        value.setTextFill(Color.web(color));

        VBox card = new VBox(12, title, value);

        card.setPadding(new Insets(18));

        card.setPrefWidth(230);

        card.setMinHeight(105);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 12;"
        );

        return card;
    }

    // CHARTS
    private void createCharts(VBox root) {

        attendanceChart = new PieChart();

        attendanceChart.setTitle(
                "Present vs Absent"
        );

        attendanceChart.setLabelsVisible(true);

        attendanceChart.setLegendVisible(true);

        attendanceChart.setPrefHeight(300);

        CategoryAxis xAxis = new CategoryAxis();

        xAxis.setLabel("Subjects");

        NumberAxis yAxis = new NumberAxis();

        yAxis.setLabel("Attendance Percentage");

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(20);

        subjectChart = new BarChart<>(xAxis, yAxis);

        subjectChart.setTitle(
                "Subject-wise Attendance"
        );

        subjectChart.setLegendVisible(false);

        subjectChart.setAnimated(false);

        subjectChart.setPrefHeight(300);

        HBox charts = new HBox(
                15,
                createPanel(attendanceChart),
                createPanel(subjectChart)
        );

        HBox.setHgrow(
                charts.getChildren().get(0),
                Priority.ALWAYS
        );

        HBox.setHgrow(
                charts.getChildren().get(1),
                Priority.ALWAYS
        );

        root.getChildren().add(charts);
    }

    // REFRESH DASHBOARD
    private void refreshDashboard() {

        int students =
                studentService.getTotalStudents();

        int subjects =
                subjectService.getTotalSubjects();

        ArrayList<Attendance> records =
                attendanceService.getAllAttendance();

        int totalRecords = records.size();

        int present = 0;
        int absent = 0;

        for (Attendance attendance : records) {

            if (attendance.isPresent()) {
                present++;
            } else {
                absent++;
            }
        }

        double percentage = 0;

        if (totalRecords > 0) {

            percentage =
                    (present * 100.0) / totalRecords;
        }

        ArrayList<ShortageAlert> riskRecords =
                alertService.getAllRiskRecords();

        int critical = 0;
        int warning = 0;
        int safe = 0;

        for (ShortageAlert record : riskRecords) {

            if ("CRITICAL".equalsIgnoreCase(record.getRiskLevel())) {
                critical++;
            } else if ("WARNING".equalsIgnoreCase(record.getRiskLevel())) {
                warning++;
            } else {
                safe++;
            }
        }

        studentValue.setText(String.valueOf(students));

        subjectValue.setText(String.valueOf(subjects));

        attendanceValue.setText(
                String.valueOf(totalRecords)
        );

        percentageValue.setText(
                String.format("%.2f%%", percentage)
        );

        shortageValue.setText(
                String.valueOf(critical + warning)
        );

        criticalValue.setText(String.valueOf(critical));

        warningValue.setText(String.valueOf(warning));

        safeValue.setText(String.valueOf(safe));

        if (adminMode) {
            updateUserStatistics();
        }

        updateAttendanceChart(present, absent);

        updateSubjectChart(records);
    }

    // UPDATE ADMIN USER STATISTICS
    private void updateUserStatistics() {

        totalUsersValue.setText(
                String.valueOf(userService.getTotalUsers())
        );

        adminUsersValue.setText(
                String.valueOf(userService.getAdminCount())
        );

        facultyUsersValue.setText(
                String.valueOf(userService.getFacultyCount())
        );

        studentUsersValue.setText(
                String.valueOf(userService.getStudentCount())
        );
    }

    // PRESENT AND ABSENT CHART
    private void updateAttendanceChart(
            int present,
            int absent) {

        attendanceChart.setData(
                FXCollections.observableArrayList(
                        new PieChart.Data(
                                "Present (" + present + ")",
                                present
                        ),
                        new PieChart.Data(
                                "Absent (" + absent + ")",
                                absent
                        )
                )
        );
    }

    // SUBJECT ANALYTICS
    private void updateSubjectChart(
            ArrayList<Attendance> records) {

        subjectChart.getData().clear();

        HashMap<String, double[]> data =
                new HashMap<>();

        for (Attendance attendance : records) {

            String code = attendance.getSubjectCode();

            double[] values = data.get(code);

            if (values == null) {

                values = new double[2];

                data.put(code, values);
            }

            values[1]++;

            if (attendance.isPresent()) {
                values[0]++;
            }
        }

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        series.setName("Attendance");

        for (Subject subject :
                subjectService.getAllSubjects()) {

            double[] values = data.get(
                    subject.getSubjectCode()
            );

            if (values == null || values[1] == 0) {
                continue;
            }

            double average =
                    values[0] * 100.0 / values[1];

            series.getData().add(
                    new XYChart.Data<>(
                            subject.getSubjectName(),
                            average
                    )
            );
        }

        subjectChart.getData().add(series);
    }

    // PANEL DESIGN
    private VBox createPanel(
            javafx.scene.Node content) {

        VBox panel = new VBox(content);

        panel.setPadding(new Insets(15));

        panel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 12;"
        );

        HBox.setHgrow(
                panel,
                Priority.ALWAYS
        );

        return panel;
    }
}