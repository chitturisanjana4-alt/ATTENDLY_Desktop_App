package service;

import java.util.ArrayList;
import java.util.Comparator;

import model.Student;
import storage.FileManager;

public class StudentService {

    private ArrayList<Student> students;

    private final AuditLogService auditService =
            new AuditLogService();

    public StudentService() {

        students = FileManager.loadStudents();

        if (students == null) {
            students = new ArrayList<>();
        }
    }

    // ADD STUDENT
    public boolean addStudent(Student student) {

        if (student == null) {
            return false;
        }

        if (isInvalid(student)) {
            return false;
        }

        if (searchStudent(student.getRollNumber()) != null) {
            return false;
        }

        students.add(student);
        saveData();

        auditService.logAction(
                "STUDENT ADDED",
                "STUDENT",
                "Roll Number: " + student.getRollNumber()
                + ", Name: " + student.getName()
                + ", Department: " + student.getDepartment()
                + ", Year: " + student.getYear()
        );

        return true;
    }

    // GET ALL STUDENTS
    public ArrayList<Student> getAllStudents() {

        return new ArrayList<>(students);
    }

    // SEARCH BY ROLL NUMBER
    public Student searchStudent(String rollNumber) {

        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return null;
        }

        for (Student student : students) {

            if (student.getRollNumber()
                    .equalsIgnoreCase(rollNumber.trim())) {

                return student;
            }
        }

        return null;
    }

    // ADVANCED SEARCH
    public ArrayList<Student> searchStudents(String keyword) {

        ArrayList<Student> result = new ArrayList<>();

        if (keyword == null) {
            return result;
        }

        keyword = keyword.trim().toLowerCase();

        for (Student student : students) {

            if (student.getRollNumber().toLowerCase().contains(keyword)
                || student.getName().toLowerCase().contains(keyword)
                || student.getDepartment().toLowerCase().contains(keyword)
                || String.valueOf(student.getYear()).contains(keyword)) {

                result.add(student);
            }
        }

        return result;
    }

    // UPDATE STUDENT
    public boolean updateStudent(
            String rollNumber,
            String name,
            String department,
            int year) {

        Student student = searchStudent(rollNumber);

        if (student == null) {
            return false;
        }

        if (name == null || name.trim().isEmpty()
            || department == null || department.trim().isEmpty()
            || year <= 0) {

            return false;
        }

        String oldName = student.getName();

        student.setName(name.trim());
        student.setDepartment(department.trim());
        student.setYear(year);

        saveData();

        auditService.logAction(
                "STUDENT UPDATED",
                "STUDENT",
                "Roll Number: " + student.getRollNumber()
                + ", Previous Name: " + oldName
                + ", Updated Name: " + student.getName()
                + ", Department: " + student.getDepartment()
                + ", Year: " + student.getYear()
        );

        return true;
    }

    // DELETE STUDENT
    public boolean deleteStudent(String rollNumber) {

        Student student = searchStudent(rollNumber);

        if (student == null) {
            return false;
        }

        String deletedRollNumber = student.getRollNumber();
        String deletedName = student.getName();

        students.remove(student);
        saveData();

        auditService.logAction(
                "STUDENT DELETED",
                "STUDENT",
                "Deleted Roll Number: " + deletedRollNumber
                + ", Deleted Name: " + deletedName
        );

        return true;
    }

    // SORT BY NAME
    public ArrayList<Student> sortByName() {

        ArrayList<Student> sorted = new ArrayList<>(students);

        sorted.sort(
            Comparator.comparing(
                Student::getName,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return sorted;
    }

    // SORT BY ROLL NUMBER
    public ArrayList<Student> sortByRollNumber() {

        ArrayList<Student> sorted = new ArrayList<>(students);

        sorted.sort(
            Comparator.comparing(
                Student::getRollNumber,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return sorted;
    }

    // SORT BY YEAR
    public ArrayList<Student> sortByYear() {

        ArrayList<Student> sorted = new ArrayList<>(students);

        sorted.sort(
            Comparator.comparingInt(Student::getYear)
        );

        return sorted;
    }

    // TOTAL STUDENTS
    public int getTotalStudents() {

        return students.size();
    }

    // VALIDATION
    private boolean isInvalid(Student student) {

        return student.getRollNumber() == null
            || student.getRollNumber().trim().isEmpty()
            || student.getName() == null
            || student.getName().trim().isEmpty()
            || student.getDepartment() == null
            || student.getDepartment().trim().isEmpty()
            || student.getYear() <= 0;
    }

    // SAVE DATA
    private void saveData() {

        FileManager.saveStudents(students);
    }
}