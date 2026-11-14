package model;

public class ExamResult {

    private String rollNumber;
    private String subjectCode;
    private String examName;

    private double maximumMarks;
    private double obtainedMarks;

    public ExamResult(
            String rollNumber,
            String subjectCode,
            String examName,
            double maximumMarks,
            double obtainedMarks) {

        this.rollNumber = rollNumber;
        this.subjectCode = subjectCode;
        this.examName = examName;
        this.maximumMarks = maximumMarks;
        this.obtainedMarks = obtainedMarks;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getExamName() {
        return examName;
    }

    public double getMaximumMarks() {
        return maximumMarks;
    }

    public double getObtainedMarks() {
        return obtainedMarks;
    }

    public double getPercentage() {

        if (maximumMarks <= 0) {
            return 0;
        }

        return (obtainedMarks / maximumMarks) * 100;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public void setMaximumMarks(double maximumMarks) {
        this.maximumMarks = maximumMarks;
    }

    public void setObtainedMarks(double obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }
}