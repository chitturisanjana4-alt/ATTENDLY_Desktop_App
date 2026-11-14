package model;

public class Subject {

    private String subjectCode;
    private String subjectName;
    private String facultyName;
    private int totalClasses;

    public Subject(String subjectCode, String subjectName,
                   String facultyName, int totalClasses) {

        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.facultyName = facultyName;
        this.totalClasses = totalClasses;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }
}