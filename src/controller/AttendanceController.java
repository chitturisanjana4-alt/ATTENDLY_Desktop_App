package controller;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Student;
import model.Subject;
import service.StudentService;
import service.SubjectService;
import service.AttendanceService;

public class AttendanceController extends BorderPane {

    private StudentService studentService = new StudentService();
    private SubjectService subjectService = new SubjectService();
    private AttendanceService attendanceService = new AttendanceService();

    private ComboBox<Student> studentBox = new ComboBox<>();
    private ComboBox<Subject> subjectBox = new ComboBox<>();

    private DatePicker datePicker = new DatePicker(LocalDate.now());

    private RadioButton presentButton = new RadioButton("Present");
    private RadioButton absentButton = new RadioButton("Absent");

    private Label resultLabel = new Label();

    public AttendanceController() {

        setPadding(new Insets(30));
        setStyle("-fx-background-color: #F4F6FA;");

        Label title = new Label("Attendance Management");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Mark and manage daily student attendance");

        VBox heading = new VBox(8, title, subtitle);

        studentBox.setPromptText("Select Student");
        subjectBox.setPromptText("Select Subject");

        studentBox.setItems(
                FXCollections.observableArrayList(
                        studentService.getAllStudents()));

        subjectBox.setItems(
                FXCollections.observableArrayList(
                        subjectService.getAllSubjects()));

        studentBox.setPrefWidth(250);
        subjectBox.setPrefWidth(250);

        studentBox.setCellFactory(list -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);

                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getRollNumber() + " - " + student.getName());
                }
            }
        });

        studentBox.setButtonCell(new ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);

                setText(empty || student == null
                        ? "Select Student"
                        : student.getRollNumber() + " - " + student.getName());
            }
        });

        subjectBox.setCellFactory(list -> new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);

                if (empty || subject == null) {
                    setText(null);
                } else {
                    setText(subject.getSubjectCode() + " - "
                            + subject.getSubjectName());
                }
            }
        });

        subjectBox.setButtonCell(new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);

                setText(empty || subject == null
                        ? "Select Subject"
                        : subject.getSubjectCode() + " - "
                        + subject.getSubjectName());
            }
        });

        ToggleGroup attendanceGroup = new ToggleGroup();

        presentButton.setToggleGroup(attendanceGroup);
        absentButton.setToggleGroup(attendanceGroup);

        presentButton.setSelected(true);

        HBox attendanceOptions = new HBox(
                25, presentButton, absentButton);

        Button markButton = new Button("Mark Attendance");

        markButton.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 25;"
        );

        markButton.setOnAction(e -> markAttendance());

        resultLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #166534;"
        );

        VBox form = new VBox(20);

        form.getChildren().addAll(
                new Label("Select Student"),
                studentBox,
                new Label("Select Subject"),
                subjectBox,
                new Label("Select Date"),
                datePicker,
                new Label("Attendance Status"),
                attendanceOptions,
                markButton,
                resultLabel
        );

        form.setPadding(new Insets(25));
        form.setMaxWidth(450);

        form.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;"
        );

        VBox content = new VBox(25, heading, form);

        setCenter(content);
    }

    private void markAttendance() {

        Student student = studentBox.getValue();
        Subject subject = subjectBox.getValue();
        LocalDate date = datePicker.getValue();

        if (student == null || subject == null || date == null) {

            showMessage("Please select student, subject and date.");
            return;
        }

        boolean present = presentButton.isSelected();

        boolean success = attendanceService.markAttendance(
                student.getRollNumber(),
                subject.getSubjectCode(),
                date,
                present
        );

        if (success) {

            resultLabel.setText(
                    "Attendance marked successfully!");

        } else {

            showMessage(
                    "Attendance already marked for this student, subject and date.");
        }
    }

    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ATTENDLY");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}