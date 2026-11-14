package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Student;
import service.StudentService;

public class StudentController extends BorderPane {

    private StudentService service = new StudentService();

    private TextField rollField = new TextField();
    private TextField nameField = new TextField();
    private TextField departmentField = new TextField();
    private TextField yearField = new TextField();
    private TextField searchField = new TextField();

    private TableView<Student> table = new TableView<>();

    private ObservableList<Student> studentList =
            FXCollections.observableArrayList();

    private Label totalLabel = new Label();

    public StudentController() {

        setPadding(new Insets(25));
        setStyle("-fx-background-color: #F4F6FA;");

        Label title = new Label("Student Management");
        title.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Advanced student registration and record management"
        );

        VBox heading = new VBox(8, title, subtitle);

        rollField.setPromptText("Roll Number");
        nameField.setPromptText("Student Name");
        departmentField.setPromptText("Department");
        yearField.setPromptText("Year");

        searchField.setPromptText(
                "Search by roll number, name or department"
        );

        rollField.setPrefWidth(160);
        nameField.setPrefWidth(180);
        departmentField.setPrefWidth(180);
        yearField.setPrefWidth(100);

        Button addButton = new Button("Add Student");
        Button editButton = new Button("Update Student");
        Button deleteButton = new Button("Delete Selected");
        Button clearButton = new Button("Clear Fields");

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");

        Button nameSortButton = new Button("Sort by Name");
        Button rollSortButton = new Button("Sort by Roll");
        Button yearSortButton = new Button("Sort by Year");

        styleButton(addButton, "#2563EB");
        styleButton(editButton, "#0F766E");
        styleButton(deleteButton, "#DC2626");
        styleButton(clearButton, "#64748B");

        HBox form = new HBox(
                10,
                rollField,
                nameField,
                departmentField,
                yearField
        );

        HBox actionButtons = new HBox(
                10,
                addButton,
                editButton,
                deleteButton,
                clearButton
        );

        HBox searchBox = new HBox(
                10,
                searchField,
                searchButton,
                refreshButton
        );

        HBox sortingBox = new HBox(
                10,
                nameSortButton,
                rollSortButton,
                yearSortButton
        );

        totalLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );

        createTable();

        addButton.setOnAction(e -> addStudent());

        editButton.setOnAction(e -> updateStudent());

        deleteButton.setOnAction(e -> deleteStudent());

        clearButton.setOnAction(e -> clearFields());

        searchButton.setOnAction(e -> searchStudent());

        refreshButton.setOnAction(e -> refreshTable());

        nameSortButton.setOnAction(e -> sortByName());

        rollSortButton.setOnAction(e -> sortByRoll());

        yearSortButton.setOnAction(e -> sortByYear());

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {

                    if (selected != null) {
                        showStudentDetails(selected);
                    }
                });

        VBox content = new VBox(
                18,
                heading,
                new Separator(),
                new Label("Student Details"),
                form,
                actionButtons,
                new Separator(),
                new Label("Search and Filter"),
                searchBox,
                new Label("Sort Records"),
                sortingBox,
                totalLabel,
                table
        );

        content.setPadding(new Insets(10));

        setCenter(content);

        refreshTable();
    }

    private void createTable() {

        TableColumn<Student, String> rollColumn =
                new TableColumn<>("Roll Number");

        rollColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getRollNumber()
                )
        );

        TableColumn<Student, String> nameColumn =
                new TableColumn<>("Student Name");

        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getName()
                )
        );

        TableColumn<Student, String> departmentColumn =
                new TableColumn<>("Department");

        departmentColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDepartment()
                )
        );

        TableColumn<Student, String> yearColumn =
                new TableColumn<>("Year");

        yearColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getYear())
                )
        );

        table.getColumns().addAll(
                rollColumn,
                nameColumn,
                departmentColumn,
                yearColumn
        );

        table.setItems(studentList);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        table.setPrefHeight(420);
        table.setPlaceholder(
                new Label("No student records available")
        );
    }

    private void addStudent() {

        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String yearText = yearField.getText().trim();

        if (roll.isEmpty() || name.isEmpty()
                || department.isEmpty() || yearText.isEmpty()) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Please fill all student details."
            );
            return;
        }

        int year;

        try {
            year = Integer.parseInt(yearText);

            if (year <= 0) {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Year must be a positive number."
                );
                return;
            }

        } catch (NumberFormatException e) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Enter a valid numeric year."
            );
            return;
        }

        Student student = new Student(
                roll,
                name,
                department,
                year
        );

        if (service.addStudent(student)) {

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Student added successfully!"
            );

            clearFields();
            refreshTable();

        } else {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid details or roll number already exists."
            );
        }
    }

    private void updateStudent() {

        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String yearText = yearField.getText().trim();

        if (roll.isEmpty() || name.isEmpty()
                || department.isEmpty() || yearText.isEmpty()) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Select a student and fill all details."
            );
            return;
        }

        int year;

        try {
            year = Integer.parseInt(yearText);

            if (year <= 0) {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Year must be a positive number."
                );
                return;
            }

        } catch (NumberFormatException e) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Enter a valid numeric year."
            );
            return;
        }

        boolean updated = service.updateStudent(
                roll,
                name,
                department,
                year
        );

        if (updated) {

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Student updated successfully!"
            );

            clearFields();
            refreshTable();

        } else {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Unable to update student."
            );
        }
    }

    private void deleteStudent() {

        Student selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Please select a student from the table."
            );
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Student Record");

        confirmation.setContentText(
                "Are you sure you want to delete " +
                selected.getName() + "?"
        );

        if (confirmation.showAndWait().orElse(
                ButtonType.CANCEL
        ) == ButtonType.OK) {

            if (service.deleteStudent(
                    selected.getRollNumber()
            )) {

                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Student deleted successfully!"
                );

                clearFields();
                refreshTable();
            }
        }
    }

    private void searchStudent() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }

        studentList.setAll(
                service.searchStudents(keyword)
        );

        updateTotal();

        if (studentList.isEmpty()) {

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "No matching student found."
            );
        }
    }

    private void sortByName() {

        studentList.setAll(
                service.sortByName()
        );

        updateTotal();
    }

    private void sortByRoll() {

        studentList.setAll(
                service.sortByRollNumber()
        );

        updateTotal();
    }

    private void sortByYear() {

        studentList.setAll(
                service.sortByYear()
        );

        updateTotal();
    }

    private void showStudentDetails(Student student) {

        rollField.setText(student.getRollNumber());
        nameField.setText(student.getName());
        departmentField.setText(student.getDepartment());
        yearField.setText(
                String.valueOf(student.getYear())
        );
    }

    private void refreshTable() {

        studentList.setAll(
                service.getAllStudents()
        );

        updateTotal();
    }

    private void updateTotal() {

        totalLabel.setText(
                "Total Students: " + studentList.size()
        );
    }

    private void clearFields() {

        rollField.clear();
        nameField.clear();
        departmentField.clear();
        yearField.clear();
        searchField.clear();

        table.getSelectionModel().clearSelection();
    }

    private void styleButton(Button button, String color) {

        button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 15 9 15;" +
                "-fx-background-radius: 6;"
        );
    }

    private void showMessage(
            Alert.AlertType type,
            String message
    ) {

        Alert alert = new Alert(type);

        alert.setTitle("ATTENDLY");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}