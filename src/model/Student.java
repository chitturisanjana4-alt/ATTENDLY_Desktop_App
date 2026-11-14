package model;

public class Student {

    private String rollNumber;
    private String name;
    private String department;
    private int year;

    // Constructor
    public Student(
            String rollNumber,
            String name,
            String department,
            int year) {

        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.year = year;
    }

    // Get Roll Number
    public String getRollNumber() {
        return rollNumber;
    }

    // Set Roll Number
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    // Get Name
    public String getName() {
        return name;
    }

    // Set Name
    public void setName(String name) {
        this.name = name;
    }

    // Get Department
    public String getDepartment() {
        return department;
    }

    // Set Department
    public void setDepartment(String department) {
        this.department = department;
    }

    // Get Year
    public int getYear() {
        return year;
    }

    // Set Year
    public void setYear(int year) {
        this.year = year;
    }
}