package controller;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import model.Attendance;
import model.Student;
import model.Subject;

import service.AttendanceService;
import service.StudentService;
import service.SubjectService;
import service.AuditLogService;

public class ReportsController extends BorderPane {

    private final StudentService studentService = new StudentService();
    private final SubjectService subjectService = new SubjectService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final AuditLogService auditService = new AuditLogService();

    private final ObservableList<ReportRow> reportData =
            FXCollections.observableArrayList();

    private final TableView<ReportRow> table = new TableView<>();

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    private final Label totalStudentsValue = new Label("0");
    private final Label totalSubjectsValue = new Label("0");
    private final Label totalRecordsValue = new Label("0");
    private final Label overallValue = new Label("0%");

    private BarChart<String, Number> attendanceChart;

    public ReportsController() {
        createUI();
        loadReports();
    }

    // CREATE UI
    private void createUI() {

        setPadding(new Insets(20));

        Label heading = new Label("ATTENDLY | Attendance Reports");

        heading.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Advanced attendance analytics and student performance"
        );

        VBox header = new VBox(5, heading, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));

        setTop(header);

        // KPI CARDS
        GridPane cards = new GridPane();
        cards.setHgap(15);
        cards.setVgap(15);

        cards.add(createCard("TOTAL STUDENTS", totalStudentsValue), 0, 0);
        cards.add(createCard("TOTAL SUBJECTS", totalSubjectsValue), 1, 0);
        cards.add(createCard("ATTENDANCE RECORDS", totalRecordsValue), 2, 0);
        cards.add(createCard("OVERALL ATTENDANCE", overallValue), 3, 0);

        // CHART
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Subject");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Attendance Percentage");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(20);

        attendanceChart = new BarChart<>(xAxis, yAxis);
        attendanceChart.setTitle("Subject-wise Attendance");
        attendanceChart.setLegendVisible(false);
        attendanceChart.setAnimated(false);
        attendanceChart.setPrefHeight(300);

        // SEARCH AND FILTER
        searchField.setPromptText(
                "Search roll number, student or subject..."
        );
        searchField.setPrefWidth(300);

        statusFilter.getItems().addAll(
                "All", "Safe", "Shortage", "Critical"
        );

        statusFilter.setValue("All");

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) -> applyFilters()
        );

        statusFilter.valueProperty().addListener(
                (obs, oldValue, newValue) -> applyFilters()
        );

        Button refreshButton = new Button("Refresh Reports");
        refreshButton.setOnAction(e -> loadReports());

        Button exportButton = new Button("Export CSV");
        exportButton.setOnAction(e -> exportCSV());

        HBox controls = new HBox(
                10, searchField, statusFilter,
                refreshButton, exportButton
        );

        controls.setPadding(new Insets(15, 0, 15, 0));

        createTable();

        VBox content = new VBox(
                18, cards, attendanceChart, controls, table
        );

        content.setPadding(new Insets(5));
        setCenter(content);
    }

    // KPI CARD
    private VBox createCard(String title, Label value) {

        Label titleLabel = new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        value.setStyle(
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        VBox card = new VBox(10, titleLabel, value);

        card.setPadding(new Insets(18));
        card.setPrefWidth(190);
        card.setMinHeight(90);

        card.setStyle(
                "-fx-background-color: #eaf0f8;" +
                "-fx-background-radius: 10;"
        );

        return card;
    }

    // CREATE TABLE
    private void createTable() {

        TableColumn<ReportRow, String> rollColumn =
                new TableColumn<>("Roll Number");

        rollColumn.setCellValueFactory(
                data -> data.getValue().rollNumberProperty()
        );

        TableColumn<ReportRow, String> nameColumn =
                new TableColumn<>("Student Name");

        nameColumn.setCellValueFactory(
                data -> data.getValue().studentNameProperty()
        );

        TableColumn<ReportRow, String> subjectColumn =
                new TableColumn<>("Subject");

        subjectColumn.setCellValueFactory(
                data -> data.getValue().subjectNameProperty()
        );

        TableColumn<ReportRow, String> attendedColumn =
                new TableColumn<>("Attended");

        attendedColumn.setCellValueFactory(
                data -> data.getValue().attendedProperty()
        );

        TableColumn<ReportRow, String> totalColumn =
                new TableColumn<>("Total Classes");

        totalColumn.setCellValueFactory(
                data -> data.getValue().totalProperty()
        );

        TableColumn<ReportRow, String> percentageColumn =
                new TableColumn<>("Attendance %");

        percentageColumn.setCellValueFactory(
                data -> data.getValue().percentageProperty()
        );

        TableColumn<ReportRow, String> statusColumn =
                new TableColumn<>("Status");

        statusColumn.setCellValueFactory(
                data -> data.getValue().statusProperty()
        );

        TableColumn<ReportRow, String> neededColumn =
                new TableColumn<>("Classes Needed");

        neededColumn.setCellValueFactory(
                data -> data.getValue().neededProperty()
        );

        table.getColumns().addAll(
                rollColumn, nameColumn, subjectColumn,
                attendedColumn, totalColumn, percentageColumn,
                statusColumn, neededColumn
        );

        table.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        table.setPrefHeight(350);
        table.setMinHeight(250);
    }

    // LOAD REPORTS
    private void loadReports() {

        reportData.clear();

        ArrayList<Student> students =
                studentService.getAllStudents();

        ArrayList<Subject> subjects =
                subjectService.getAllSubjects();

        ArrayList<Attendance> records =
                attendanceService.getAllAttendance();

        totalStudentsValue.setText(String.valueOf(students.size()));
        totalSubjectsValue.setText(String.valueOf(subjects.size()));
        totalRecordsValue.setText(String.valueOf(records.size()));

        int presentCount = 0;

        for (Attendance attendance : records) {
            if (attendance.isPresent()) {
                presentCount++;
            }
        }

        double overall = records.isEmpty()
                ? 0
                : (presentCount * 100.0) / records.size();

        overallValue.setText(String.format("%.1f%%", overall));

        Map<String, int[]> attendanceMap = new HashMap<>();

        for (Attendance attendance : records) {

            String key = attendance.getRollNumber() + "|"
                    + attendance.getSubjectCode();

            int[] counts = attendanceMap.getOrDefault(
                    key, new int[]{0, 0}
            );

            counts[0]++;

            if (attendance.isPresent()) {
                counts[1]++;
            }

            attendanceMap.put(key, counts);
        }

        for (Map.Entry<String, int[]> entry :
                attendanceMap.entrySet()) {

            String[] keys = entry.getKey().split("\\|", -1);

            String rollNumber = keys[0];
            String subjectCode = keys[1];

            int total = entry.getValue()[0];
            int attended = entry.getValue()[1];

            Student foundStudent = null;
            Subject foundSubject = null;

            for (Student student : students) {
                if (student.getRollNumber().equals(rollNumber)) {
                    foundStudent = student;
                    break;
                }
            }

            for (Subject subject : subjects) {
                if (subject.getSubjectCode().equals(subjectCode)) {
                    foundSubject = subject;
                    break;
                }
            }

            if (foundStudent == null || foundSubject == null) {
                continue;
            }

            double percentage =
                    total == 0 ? 0 : attended * 100.0 / total;

            String status;

            if (percentage < 50) {
                status = "Critical";
            } else if (percentage < 75) {
                status = "Shortage";
            } else {
                status = "Safe";
            }

            int needed = 0;

            if (percentage < 75) {
                needed = (int) Math.ceil(
                        (0.75 * total - attended) / 0.25
                );
            }

            reportData.add(new ReportRow(
                    rollNumber,
                    foundStudent.getName(),
                    foundSubject.getSubjectName(),
                    attended,
                    total,
                    percentage,
                    status,
                    needed
            ));
        }

        updateChart(records, subjects);
        applyFilters();
    }// UPDATE CHART
    private void updateChart(
            ArrayList<Attendance> records,
            ArrayList<Subject> subjects) {

        attendanceChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> series =
                new javafx.scene.chart.XYChart.Series<>();

        for (Subject subject : subjects) {

            int total = 0;
            int present = 0;

            for (Attendance attendance : records) {

                if (attendance.getSubjectCode().equals(
                        subject.getSubjectCode())) {

                    total++;

                    if (attendance.isPresent()) {
                        present++;
                    }
                }
            }

            if (total > 0) {

                double percentage = present * 100.0 / total;

                series.getData().add(
                        new javafx.scene.chart.XYChart.Data<>(
                                subject.getSubjectName(),
                                percentage
                        )
                );
            }
        }

        attendanceChart.getData().add(series);
    }

    // SEARCH AND FILTER
    private void applyFilters() {

        ObservableList<ReportRow> filtered =
                FXCollections.observableArrayList();

        String search = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();

        String filter = statusFilter.getValue();

        for (ReportRow row : reportData) {

            boolean matchesSearch =
                    row.getRollNumber().toLowerCase().contains(search)
                    || row.getStudentName().toLowerCase().contains(search)
                    || row.getSubjectName().toLowerCase().contains(search);

            boolean matchesStatus =
                    filter == null
                    || filter.equals("All")
                    || filter.equals(row.getStatus());

            if (matchesSearch && matchesStatus) {
                filtered.add(row);
            }
        }

        table.setItems(filtered);
    }

    // EXPORT CSV
    private void exportCSV() {

        if (table.getItems().isEmpty()) {

            showAlert(
                    "No Data",
                    "There are no report records to export."
            );

            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Export Attendance Report");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files", "*.csv"
                )
        );

        chooser.setInitialFileName("ATTENDLY_Report.csv");

        Window window = getScene() == null
                ? null
                : getScene().getWindow();

        File file = chooser.showSaveDialog(window);

        if (file == null) {
            return;
        }

        int exportedRecords = table.getItems().size();

        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println(
                    "Roll Number,Student Name,Subject,Attended,"
                    + "Total Classes,Attendance Percentage,Status,Classes Needed"
            );

            for (ReportRow row : table.getItems()) {

                writer.println(
                        csv(row.getRollNumber()) + ","
                        + csv(row.getStudentName()) + ","
                        + csv(row.getSubjectName()) + ","
                        + row.getAttended() + ","
                        + row.getTotal() + ","
                        + String.format("%.2f", row.getPercentage()) + ","
                        + csv(row.getStatus()) + ","
                        + row.getNeeded()
                );
            }

            if (writer.checkError()) {
                throw new IOException("Unable to write CSV file.");
            }

            auditService.logAction(
                    "ATTENDANCE REPORT EXPORTED",
                    "REPORT",
                    "Records exported: " + exportedRecords
                    + ", File: " + file.getName()
            );

            showAlert(
                    "Export Successful",
                    "Your attendance report was exported successfully."
            );

        } catch (Exception ex) {

            showAlert(
                    "Export Failed",
                    ex.getMessage()
            );
        }
    }

    // CSV VALUE FORMATTING
    private String csv(String value) {

        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ALERT
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // REPORT ROW MODEL
    public static class ReportRow {

        private final javafx.beans.property.SimpleStringProperty rollNumber;
        private final javafx.beans.property.SimpleStringProperty studentName;
        private final javafx.beans.property.SimpleStringProperty subjectName;
        private final javafx.beans.property.SimpleStringProperty attended;
        private final javafx.beans.property.SimpleStringProperty total;
        private final javafx.beans.property.SimpleStringProperty percentage;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty needed;

        private final int attendedCount;
        private final int totalCount;
        private final double percentageValue;
        private final int neededCount;

        public ReportRow(
                String rollNumber,
                String studentName,
                String subjectName,
                int attended,
                int total,
                double percentage,
                String status,
                int needed) {

            this.rollNumber =
                    new javafx.beans.property.SimpleStringProperty(rollNumber);

            this.studentName =
                    new javafx.beans.property.SimpleStringProperty(studentName);

            this.subjectName =
                    new javafx.beans.property.SimpleStringProperty(subjectName);

            this.attended =
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(attended)
                    );

            this.total =
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(total)
                    );

            this.percentage =
                    new javafx.beans.property.SimpleStringProperty(
                            String.format("%.1f%%", percentage)
                    );

            this.status =
                    new javafx.beans.property.SimpleStringProperty(status);

            this.needed =
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(needed)
                    );

            this.attendedCount = attended;
            this.totalCount = total;
            this.percentageValue = percentage;
            this.neededCount = needed;
        }

        public javafx.beans.property.StringProperty rollNumberProperty() {
            return rollNumber;
        }

        public javafx.beans.property.StringProperty studentNameProperty() {
            return studentName;
        }

        public javafx.beans.property.StringProperty subjectNameProperty() {
            return subjectName;
        }

        public javafx.beans.property.StringProperty attendedProperty() {
            return attended;
        }

        public javafx.beans.property.StringProperty totalProperty() {
            return total;
        }

        public javafx.beans.property.StringProperty percentageProperty() {
            return percentage;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.StringProperty neededProperty() {
            return needed;
        }

        public String getRollNumber() {
            return rollNumber.get();
        }

        public String getStudentName() {
            return studentName.get();
        }

        public String getSubjectName() {
            return subjectName.get();
        }

        public String getStatus() {
            return status.get();
        }

        public int getAttended() {
            return attendedCount;
        }

        public int getTotal() {
            return totalCount;
        }

        public double getPercentage() {
            return percentageValue;
        }

        public int getNeeded() {
            return neededCount;
        }
    }
}