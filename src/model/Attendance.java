package model;

import java.time.LocalDate;

public class Attendance {

    private String rollNumber;
    private String subjectCode;
    private LocalDate date;
    private boolean present;

    public Attendance(String rollNumber, String subjectCode,
                      LocalDate date, boolean present) {

        this.rollNumber = rollNumber;
        this.subjectCode = subjectCode;
        this.date = date;
        this.present = present;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}