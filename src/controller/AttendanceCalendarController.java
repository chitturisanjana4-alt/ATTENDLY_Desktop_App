package controller;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import model.Attendance;
import model.Student;
import model.Subject;

import service.AttendanceService;
import service.StudentService;
import service.SubjectService;

public class AttendanceCalendarController extends BorderPane {

    private AttendanceService attendanceService;
    private StudentService studentService;
    private SubjectService subjectService;

    private YearMonth currentMonth;
    private LocalDate selectedDate;

    private GridPane calendarGrid;

    private Label monthLabel;
    private Label selectedDateLabel;

    private Label totalLabel;
    private Label presentLabel;
    private Label absentLabel;
    private Label percentageLabel;

    private VBox detailsBox;

    private ComboBox<String> subjectFilter;
    private TextField searchField;

    private Map<LocalDate, int[]> dailySummary = new HashMap<>();

    private final DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    public AttendanceCalendarController() {

        attendanceService = new AttendanceService();
        studentService = new StudentService();
        subjectService = new SubjectService();

        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        createUI();
        loadSubjectFilter();
        refreshCalendar();
        showSelectedDate();
    }

    // ================= MAIN UI =================

    private void createUI() {

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #F4F6FA;");

        Label heading = new Label("Attendance Calendar");

        heading.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        heading.setTextFill(Color.web("#172554"));

        Label subtitle = new Label(
            "Monthly attendance tracking and daily activity overview"
        );

        subtitle.setTextFill(Color.web("#64748B"));

        VBox header = new VBox(6, heading, subtitle);

        HBox top = new HBox(header);
        top.setPadding(new Insets(0, 0, 20, 0));

        setTop(top);

        VBox mainContent = new VBox(18);

        HBox summaryCards = createSummaryCards();

        VBox calendarSection = createCalendarSection();

        VBox detailsSection = createDetailsSection();

        mainContent.getChildren().addAll(
            summaryCards,
            calendarSection,
            detailsSection
        );

        ScrollPane scrollPane = new ScrollPane(mainContent);

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F4F6FA;");

        setCenter(scrollPane);
    }

    // ================= SUMMARY CARDS =================

    private HBox createSummaryCards() {

        totalLabel = new Label("0");
        presentLabel = new Label("0");
        absentLabel = new Label("0");
        percentageLabel = new Label("0%");

        HBox cards = new HBox(12);

        cards.getChildren().addAll(
            createCard("TOTAL RECORDS", totalLabel, "#2563EB"),
            createCard("PRESENT", presentLabel, "#16A34A"),
            createCard("ABSENT", absentLabel, "#DC2626"),
            createCard("ATTENDANCE", percentageLabel, "#7C3AED")
        );

        return cards;
    }

    private VBox createCard(
        String title,
        Label value,
        String color
    ) {

        Label heading = new Label(title);

        heading.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        heading.setTextFill(Color.web("#64748B"));

        value.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        value.setTextFill(Color.web(color));

        VBox card = new VBox(12, heading, value);

        card.setPadding(new Insets(18));
        card.setPrefWidth(190);
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #E2E8F0;" +
            "-fx-border-radius: 12;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // ================= CALENDAR SECTION =================

    private VBox createCalendarSection() {

        VBox section = new VBox(15);

        section.setPadding(new Insets(20));

        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;"
        );

        Button previousButton = new Button("◀ Previous");

        Button nextButton = new Button("Next ▶");

        monthLabel = new Label();

        monthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 19));

        HBox monthNavigation = new HBox(
            15,
            previousButton,
            monthLabel,
            nextButton
        );

        monthNavigation.setAlignment(Pos.CENTER);

        previousButton.setOnAction(e -> {

            currentMonth = currentMonth.minusMonths(1);

            refreshCalendar();
        });

        nextButton.setOnAction(e -> {

            currentMonth = currentMonth.plusMonths(1);

            refreshCalendar();
        });

        calendarGrid = new GridPane();

        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);

        calendarGrid.setAlignment(Pos.CENTER);

        String[] days = {
            "MON", "TUE", "WED", "THU",
            "FRI", "SAT", "SUN"
        };

        for (int i = 0; i < days.length; i++) {

            Label day = new Label(days[i]);

            day.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            day.setTextFill(Color.web("#64748B"));

            day.setAlignment(Pos.CENTER);

            day.setMinSize(85, 30);
            day.setMaxWidth(Double.MAX_VALUE);

            calendarGrid.add(day, i, 0);
        }

        section.getChildren().addAll(
            monthNavigation,
            calendarGrid
        );

        return section;
    }

    // ================= CALENDAR GENERATION =================

    private void refreshCalendar() {

        monthLabel.setText(
            currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")
            )
        );

        dailySummary.clear();

        buildDailySummary();

        calendarGrid.getChildren().removeIf(node -> {

            Integer row = GridPane.getRowIndex(node);

            return row != null && row > 0;
        });

        LocalDate firstDay = currentMonth.atDay(1);

        int startColumn = firstDay.getDayOfWeek().getValue() - 1;

        int daysInMonth = currentMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {

            LocalDate date = currentMonth.atDay(day);

            int column = (startColumn + day - 1) % 7;

            int row = (startColumn + day - 1) / 7 + 1;

            VBox dayCell = createDayCell(date);

            calendarGrid.add(dayCell, column, row);
        }
    }

    private void buildDailySummary() {

        for (Attendance attendance :
            attendanceService.getAllAttendance()) {

            LocalDate date = attendance.getDate();

            int[] counts = dailySummary.computeIfAbsent(
                date,
                key -> new int[3]
            );

            counts[0]++;

            if (attendance.isPresent()) {
                counts[1]++;
            } else {
                counts[2]++;
            }
        }
    }

    // ================= DAY CELL =================

    private VBox createDayCell(LocalDate date) {

        Label dayNumber = new Label(
            String.valueOf(date.getDayOfMonth())
        );

        dayNumber.setFont(
            Font.font("Arial", FontWeight.BOLD, 15)
        );

        int[] counts = dailySummary.getOrDefault(
            date,
            new int[] {0, 0, 0}
        );

        Label recordLabel = new Label(
            counts[0] + " records"
        );

        recordLabel.setFont(Font.font("Arial", 10));

        Label presentText = new Label(
            "P: " + counts[1]
        );

        presentText.setTextFill(Color.web("#16A34A"));

        Label absentText = new Label(
            "A: " + counts[2]
        );

        absentText.setTextFill(Color.web("#DC2626"));

        VBox cell = new VBox(
            5,
            dayNumber,
            recordLabel,
            presentText,
            absentText
        );

        cell.setAlignment(Pos.CENTER);

        cell.setPadding(new Insets(8));

        cell.setMinSize(85, 85);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        String background = date.equals(selectedDate)
            ? "#DBEAFE"
            : "#F8FAFC";

        String border = date.equals(LocalDate.now())
            ? "#2563EB"
            : "#E2E8F0";

        cell.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        cell.setOnMouseClicked(e -> {

            selectedDate = date;

            refreshCalendar();

            showSelectedDate();
        });

        return cell;
    }

    // ================= DETAILS SECTION =================

    private VBox createDetailsSection() {

        VBox section = new VBox(15);

        section.setPadding(new Insets(20));

        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;"
        );

        selectedDateLabel = new Label("Selected Date");

        selectedDateLabel.setFont(
            Font.font("Arial", FontWeight.BOLD, 19)
        );

        searchField = new TextField();

        searchField.setPromptText(
            "Search by roll number or student name"
        );

        subjectFilter = new ComboBox<>();

        subjectFilter.setPromptText("Filter by subject");

        Button exportButton = new Button("Export Daily CSV");

        exportButton.setOnAction(e -> exportDailyCSV());

        HBox filters = new HBox(
            10,
            searchField,
            subjectFilter,
            exportButton
        );

        filters.setAlignment(Pos.CENTER_LEFT);

        searchField.setPrefWidth(280);

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> showSelectedDate()
        );

        subjectFilter.setOnAction(e -> showSelectedDate());

        detailsBox = new VBox(8);

        section.getChildren().addAll(
            selectedDateLabel,
            filters,
            detailsBox
        );

        return section;
    }

    // ================= SUBJECT FILTER =================

    private void loadSubjectFilter() {

        subjectFilter.getItems().clear();

        subjectFilter.getItems().add("All Subjects");

        for (Subject subject : subjectService.getAllSubjects()) {

            subjectFilter.getItems().add(
                subject.getSubjectCode()
            );
        }

        subjectFilter.setValue("All Subjects");
    }

    // ================= DAILY RECORDS =================

    private void showSelectedDate() {

        if (detailsBox == null) {
            return;
        }

        selectedDateLabel.setText(
            "Attendance on " + selectedDate.format(dateFormatter)
        );

        detailsBox.getChildren().clear();

        int total = 0;
        int present = 0;
        int absent = 0;

        String search = searchField.getText()
            .trim()
            .toLowerCase();

        String selectedSubject = subjectFilter.getValue();

        ArrayList<Attendance> records =
            attendanceService.getAllAttendance();

        for (Attendance attendance : records) {

            if (!attendance.getDate().equals(selectedDate)) {
                continue;
            }

            if (selectedSubject != null
                && !selectedSubject.equals("All Subjects")
                && !attendance.getSubjectCode().equals(selectedSubject)) {

                continue;
            }

            Student student = findStudent(
                attendance.getRollNumber()
            );

            if (student == null) {
                continue;
            }

            boolean matchesSearch =
                student.getRollNumber().toLowerCase().contains(search)
                || student.getName().toLowerCase().contains(search);

            if (!matchesSearch) {
                continue;
            }

            total++;

            if (attendance.isPresent()) {
                present++;
            } else {
                absent++;
            }

            addAttendanceRow(attendance, student);
        }

        totalLabel.setText(String.valueOf(total));

        presentLabel.setText(String.valueOf(present));

        absentLabel.setText(String.valueOf(absent));

        double percentage = total == 0
            ? 0
            : (present * 100.0) / total;

        percentageLabel.setText(
            String.format("%.1f%%", percentage)
        );

        if (total == 0) {

            Label emptyLabel = new Label(
                "No attendance records found for this date."
            );

            emptyLabel.setTextFill(Color.web("#64748B"));

            detailsBox.getChildren().add(emptyLabel);
        }
    }

    // ================= STUDENT LOOKUP =================

    private Student findStudent(String rollNumber) {

        for (Student student : studentService.getAllStudents()) {

            if (student.getRollNumber().equalsIgnoreCase(rollNumber)) {
                return student;
            }
        }

        return null;
    }

    // ================= ATTENDANCE ROW =================

    private void addAttendanceRow(
        Attendance attendance,
        Student student
    ) {

        String subjectName = attendance.getSubjectCode();

        for (Subject subject : subjectService.getAllSubjects()) {

            if (subject.getSubjectCode().equalsIgnoreCase(
                attendance.getSubjectCode()
            )) {

                subjectName = subject.getSubjectName();

                break;
            }
        }

        Label roll = new Label(student.getRollNumber());

        Label name = new Label(student.getName());

        Label subject = new Label(subjectName);

        Label status = new Label(
            attendance.isPresent() ? "PRESENT" : "ABSENT"
        );

        status.setTextFill(
            attendance.isPresent()
                ? Color.web("#16A34A")
                : Color.web("#DC2626")
        );

        status.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox row = new HBox(
            20,
            roll,
            name,
            subject,
            status
        );

        row.setAlignment(Pos.CENTER_LEFT);

        row.setPadding(new Insets(12));

        row.setStyle(
            "-fx-background-color: #F8FAFC;" +
            "-fx-background-radius: 8;"
        );

        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(subject, Priority.ALWAYS);

        detailsBox.getChildren().add(row);
    }

    // ================= CSV EXPORT =================

    private void exportDailyCSV() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Export Daily Attendance");

        fileChooser.setInitialFileName(
            "Attendance_" + selectedDate + ".csv"
        );

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "CSV Files",
                "*.csv"
            )
        );

        Window window = getScene() == null
            ? null
            : getScene().getWindow();

        File file = fileChooser.showSaveDialog(window);

        if (file == null) {
            return;
        }

        String search = searchField.getText()
            .trim()
            .toLowerCase();

        String selectedSubject = subjectFilter.getValue();

        int exported = 0;

        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println(
                "Date,Roll Number,Student Name,Subject Code,Status"
            );

            for (Attendance attendance :
                attendanceService.getAllAttendance()) {

                if (!attendance.getDate().equals(selectedDate)) {
                    continue;
                }

                if (selectedSubject != null
                    && !selectedSubject.equals("All Subjects")
                    && !attendance.getSubjectCode().equals(selectedSubject)) {

                    continue;
                }

                Student student = findStudent(
                    attendance.getRollNumber()
                );

                if (student == null) {
                    continue;
                }

                boolean matchesSearch =
                    student.getRollNumber().toLowerCase().contains(search)
                    || student.getName().toLowerCase().contains(search);

                if (!matchesSearch) {
                    continue;
                }

                writer.println(
                    csv(selectedDate.toString()) + ","
                    + csv(student.getRollNumber()) + ","
                    + csv(student.getName()) + ","
                    + csv(attendance.getSubjectCode()) + ","
                    + (attendance.isPresent() ? "PRESENT" : "ABSENT")
                );

                exported++;
            }

            showMessage(
                "Export Successful",
                exported + " attendance records exported."
            );

        } catch (Exception ex) {

            showMessage(
                "Export Error",
                ex.getMessage()
            );
        }
    }

    private String csv(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // ================= ALERT =================

    private void showMessage(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}