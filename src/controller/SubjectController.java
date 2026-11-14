package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Subject;
import service.SubjectService;

public class SubjectController extends BorderPane {

    private SubjectService service = new SubjectService();

    private TextField codeField = new TextField();
    private TextField nameField = new TextField();
    private TextField facultyField = new TextField();
    private TextField totalClassesField = new TextField();
    private TextField searchField = new TextField();

    private TableView<Subject> subjectTable = new TableView<>();

    private ObservableList<Subject> subjectList =
            FXCollections.observableArrayList();

    private Label totalLabel = new Label();

    private Button addButton;
    private Button updateButton;
    private Button deleteButton;
    private Button clearButton;

    public SubjectController() {

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #F4F6FA;");

        createUI();
        createTable();
        refreshTable();
    }

    // MAIN UI
    private void createUI() {

        Label heading = new Label("Subject Management");

        heading.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label(
            "Manage subjects, faculty allocation and class information"
        );

        subtitle.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #64748B;"
        );

        VBox header = new VBox(5, heading, subtitle);
        header.setPadding(new Insets(0, 0, 20, 0));

        // FORM
        GridPane form = new GridPane();

        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        form.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;"
        );

        addFieldStyle(codeField);
        addFieldStyle(nameField);
        addFieldStyle(facultyField);
        addFieldStyle(totalClassesField);

        codeField.setPromptText("Example: CS301");
        nameField.setPromptText("Subject name");
        facultyField.setPromptText("Faculty name");
        totalClassesField.setPromptText("Total classes");

        form.add(createLabel("Subject Code"), 0, 0);
        form.add(codeField, 0, 1);

        form.add(createLabel("Subject Name"), 1, 0);
        form.add(nameField, 1, 1);

        form.add(createLabel("Faculty Name"), 2, 0);
        form.add(facultyField, 2, 1);

        form.add(createLabel("Total Classes"), 3, 0);
        form.add(totalClassesField, 3, 1);

        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        column.setPercentWidth(25);

        form.getColumnConstraints().addAll(
            column, column, column, column
        );

        // BUTTONS
        addButton = createButton("Add Subject", "#2563EB");
        updateButton = createButton("Update Subject", "#0891B2");
        deleteButton = createButton("Delete Selected", "#DC2626");
        clearButton = createButton("Clear Form", "#64748B");

        addButton.setOnAction(e -> addSubject());
        updateButton.setOnAction(e -> updateSubject());
        deleteButton.setOnAction(e -> deleteSubject());
        clearButton.setOnAction(e -> clearFields());

        HBox buttonBox = new HBox(
            10,
            addButton,
            updateButton,
            deleteButton,
            clearButton
        );

        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(15, 0, 15, 0));

        // SEARCH
        searchField.setPromptText(
            "Search by code, subject, faculty or classes..."
        );

        searchField.setPrefWidth(350);
        searchField.setStyle(
            "-fx-padding: 10;" +
            "-fx-background-radius: 7;"
        );

        Button searchButton = createButton("Search", "#2563EB");
        Button refreshButton = createButton("Refresh", "#475569");

        searchButton.setOnAction(e -> searchSubject());
        refreshButton.setOnAction(e -> {
            searchField.clear();
            refreshTable();
        });

        searchField.setOnAction(e -> searchSubject());

        HBox searchBox = new HBox(
            10,
            searchField,
            searchButton,
            refreshButton
        );

        searchBox.setAlignment(Pos.CENTER_LEFT);

        // SORT BUTTONS
        Button sortCodeButton = createButton("Sort by Code", "#475569");
        Button sortNameButton = createButton("Sort by Name", "#475569");
        Button sortClassesButton = createButton("Sort by Classes", "#475569");

        sortCodeButton.setOnAction(e -> sortByCode());
        sortNameButton.setOnAction(e -> sortByName());
        sortClassesButton.setOnAction(e -> sortByClasses());

        HBox sortBox = new HBox(
            10,
            sortCodeButton,
            sortNameButton,
            sortClassesButton
        );

        sortBox.setAlignment(Pos.CENTER_LEFT);
        sortBox.setPadding(new Insets(12, 0, 12, 0));

        totalLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #172554;"
        );

        VBox topContent = new VBox(
            header,
            form,
            buttonBox,
            searchBox,
            sortBox,
            totalLabel
        );

        setTop(topContent);
    }

    // CREATE TABLE
    private void createTable() {

        TableColumn<Subject, String> codeColumn =
                new TableColumn<>("Subject Code");

        codeColumn.setCellValueFactory(
            new PropertyValueFactory<>("subjectCode")
        );

        TableColumn<Subject, String> nameColumn =
                new TableColumn<>("Subject Name");

        nameColumn.setCellValueFactory(
            new PropertyValueFactory<>("subjectName")
        );

        TableColumn<Subject, String> facultyColumn =
                new TableColumn<>("Faculty Name");

        facultyColumn.setCellValueFactory(
            new PropertyValueFactory<>("facultyName")
        );

        TableColumn<Subject, Integer> classesColumn =
                new TableColumn<>("Total Classes");

        classesColumn.setCellValueFactory(
            new PropertyValueFactory<>("totalClasses")
        );

        codeColumn.setPrefWidth(150);
        nameColumn.setPrefWidth(250);
        facultyColumn.setPrefWidth(220);
        classesColumn.setPrefWidth(150);

        subjectTable.getColumns().addAll(
            codeColumn,
            nameColumn,
            facultyColumn,
            classesColumn
        );

        subjectTable.setItems(subjectList);
        subjectTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        subjectTable.setPlaceholder(
            new Label("No subjects available")
        );

        subjectTable.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;"
        );

        subjectTable.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {

                if (newValue != null) {
                    showSubjectDetails(newValue);
                }
            });

        VBox tableContainer = new VBox(subjectTable);
        tableContainer.setPadding(new Insets(10, 0, 0, 0));

        VBox.setVgrow(subjectTable, Priority.ALWAYS);
        setCenter(tableContainer);
    }

    // ADD SUBJECT
    private void addSubject() {

        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String faculty = facultyField.getText().trim();
        String classesText = totalClassesField.getText().trim();

        if (code.isEmpty() || name.isEmpty()
                || faculty.isEmpty() || classesText.isEmpty()) {

            showMessage(
                Alert.AlertType.WARNING,
                "Validation Error",
                "Please fill all subject details."
            );

            return;
        }

        int totalClasses;

        try {
            totalClasses = Integer.parseInt(classesText);

            if (totalClasses < 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            showMessage(
                Alert.AlertType.ERROR,
                "Invalid Input",
                "Total classes must be a non-negative whole number."
            );

            return;
        }

        Subject subject = new Subject(
            code,
            name,
            faculty,
            totalClasses
        );

        if (service.addSubject(subject)) {

            refreshTable();
            clearFields();

            showMessage(
                Alert.AlertType.INFORMATION,
                "Success",
                "Subject added successfully."
            );

        } else {

            showMessage(
                Alert.AlertType.WARNING,
                "Unable to Add",
                "Subject already exists or details are invalid."
            );
        }
    }

    // UPDATE SUBJECT
    private void updateSubject() {

        Subject selected =
                subjectTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showMessage(
                Alert.AlertType.WARNING,
                "Select Subject",
                "Select a subject from the table before updating."
            );

            return;
        }

        String code = selected.getSubjectCode();
        String name = nameField.getText().trim();
        String faculty = facultyField.getText().trim();
        String classesText = totalClassesField.getText().trim();

        if (name.isEmpty() || faculty.isEmpty() || classesText.isEmpty()) {

            showMessage(
                Alert.AlertType.WARNING,
                "Validation Error",
                "Please enter all subject details."
            );

            return;
        }

        int totalClasses;

        try {

            totalClasses = Integer.parseInt(classesText);

            if (totalClasses < 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            showMessage(
                Alert.AlertType.ERROR,
                "Invalid Input",
                "Total classes must be a non-negative whole number."
            );

            return;
        }

        boolean updated = service.updateSubject(
            code,
            name,
            faculty,
            totalClasses
        );

        if (updated) {

            refreshTable();
            clearFields();

            showMessage(
                Alert.AlertType.INFORMATION,
                "Updated",
                "Subject updated successfully."
            );

        } else {

            showMessage(
                Alert.AlertType.ERROR,
                "Update Failed",
                "Unable to update subject."
            );
        }
    }

    // DELETE SUBJECT
    private void deleteSubject() {

        Subject selected =
                subjectTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showMessage(
                Alert.AlertType.WARNING,
                "Select Subject",
                "Please select a subject to delete."
            );

            return;
        }

        Alert confirmation = new Alert(
            Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete Subject");
        confirmation.setHeaderText("Confirm subject deletion");

        confirmation.setContentText(
            "Are you sure you want to delete "
            + selected.getSubjectName() + "?"
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                if (service.deleteSubject(
                        selected.getSubjectCode())) {

                    refreshTable();
                    clearFields();

                    showMessage(
                        Alert.AlertType.INFORMATION,
                        "Deleted",
                        "Subject deleted successfully."
                    );
                }
            }
        });
    }

    // SEARCH SUBJECT
    private void searchSubject() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }

        subjectList.setAll(
            service.searchSubjects(keyword)
        );

        updateTotal();

        if (subjectList.isEmpty()) {

            showMessage(
                Alert.AlertType.INFORMATION,
                "No Results",
                "No matching subjects found."
            );
        }
    }

    // SORT BY CODE
    private void sortByCode() {

        subjectList.setAll(service.sortByCode());
        updateTotal();
    }

    // SORT BY NAME
    private void sortByName() {

        subjectList.setAll(service.sortByName());
        updateTotal();
    }

    // SORT BY CLASSES
    private void sortByClasses() {

        subjectList.setAll(service.sortByClasses());
        updateTotal();
    }

    // REFRESH TABLE
    private void refreshTable() {

        subjectList.setAll(service.getAllSubjects());
        updateTotal();
    }

    // SHOW SELECTED SUBJECT DETAILS
    private void showSubjectDetails(Subject subject) {

        codeField.setText(subject.getSubjectCode());
        nameField.setText(subject.getSubjectName());
        facultyField.setText(subject.getFacultyName());

        totalClassesField.setText(
            String.valueOf(subject.getTotalClasses())
        );
    }

    // CLEAR FORM
    private void clearFields() {

        codeField.clear();
        nameField.clear();
        facultyField.clear();
        totalClassesField.clear();

        subjectTable.getSelectionModel().clearSelection();
    }

    // UPDATE COUNTER
    private void updateTotal() {

        totalLabel.setText(
            "Total Subjects: " + subjectList.size()
        );
    }

    // LABEL
    private Label createLabel(String text) {

        Label label = new Label(text);

        label.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #334155;"
        );

        return label;
    }

    // FIELD STYLE
    private void addFieldStyle(TextField field) {

        field.setMaxWidth(Double.MAX_VALUE);

        field.setStyle(
            "-fx-padding: 10;" +
            "-fx-background-radius: 6;" +
            "-fx-border-radius: 6;" +
            "-fx-border-color: #CBD5E1;"
        );
    }

    // BUTTON STYLE
    private Button createButton(String text, String color) {

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