package service;

import java.util.ArrayList;
import java.util.Comparator;

import model.Student;
import model.Subject;

public class ShortageAlertService {

    private static final double REQUIRED_PERCENTAGE = 75.0;

    private StudentService studentService;
    private SubjectService subjectService;
    private AttendanceService attendanceService;

    private final AuditLogService auditService = new AuditLogService();

    public ShortageAlertService() {

        studentService = new StudentService();
        subjectService = new SubjectService();
        attendanceService = new AttendanceService();
    }

    // ADVANCED SHORTAGE REPORT MODEL
    public static class ShortageAlert {

        private String rollNumber;
        private String studentName;
        private String subjectName;

        private int attendedClasses;
        private int totalClasses;

        private double percentage;
        private int requiredClasses;

        private String riskLevel;
        private double riskScore;
        private String recommendation;

        public ShortageAlert(
                String rollNumber,
                String studentName,
                String subjectName,
                int attendedClasses,
                int totalClasses,
                double percentage,
                int requiredClasses,
                String riskLevel,
                double riskScore,
                String recommendation) {

            this.rollNumber = rollNumber;
            this.studentName = studentName;
            this.subjectName = subjectName;
            this.attendedClasses = attendedClasses;
            this.totalClasses = totalClasses;
            this.percentage = percentage;
            this.requiredClasses = requiredClasses;
            this.riskLevel = riskLevel;
            this.riskScore = riskScore;
            this.recommendation = recommendation;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public int getAttendedClasses() {
            return attendedClasses;
        }

        public int getTotalClasses() {
            return totalClasses;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getRequiredClasses() {
            return requiredClasses;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public double getRiskScore() {
            return riskScore;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    // CALCULATE REQUIRED FUTURE CLASSES
    private int calculateRequiredClasses(int attended, int total) {

        double required =
                (REQUIRED_PERCENTAGE * total - attended)
                / (100.0 - REQUIRED_PERCENTAGE);

        return Math.max(0, (int) Math.ceil(required));
    }

    // CALCULATE RISK SCORE
    private double calculateRiskScore(double percentage) {

        double score = 100.0 - percentage;

        return Math.max(0, Math.min(100, score));
    }

    // IDENTIFY RISK LEVEL
    private String getRiskLevel(double percentage) {

        if (percentage < 50) {
            return "CRITICAL";
        }

        if (percentage < REQUIRED_PERCENTAGE) {
            return "WARNING";
        }

        return "SAFE";
    }

    // PROVIDE RECOMMENDATION
    private String getRecommendation(
            String riskLevel, int requiredClasses) {

        switch (riskLevel) {

            case "CRITICAL":
                return "Immediate attention required. Attend upcoming classes consistently.";

            case "WARNING":
                return "Monitor attendance and attend the next "
                        + requiredClasses + " classes.";

            default:
                return "Attendance is meeting the required threshold.";
        }
    }

    // GENERATE ALL STUDENT RISK RECORDS
    public ArrayList<ShortageAlert> getAllRiskRecords() {

        ArrayList<ShortageAlert> alerts = new ArrayList<>();

        ArrayList<Student> students = studentService.getAllStudents();
        ArrayList<Subject> subjects = subjectService.getAllSubjects();

        for (Student student : students) {

            for (Subject subject : subjects) {

                int total = attendanceService.getTotalClasses(
                        student.getRollNumber(),
                        subject.getSubjectCode()
                );

                int attended = attendanceService.getAttendedClasses(
                        student.getRollNumber(),
                        subject.getSubjectCode()
                );

                if (total == 0) {
                    continue;
                }

                double percentage = attended * 100.0 / total;

                int requiredClasses =
                        calculateRequiredClasses(attended, total);

                String riskLevel = getRiskLevel(percentage);

                double riskScore = calculateRiskScore(percentage);

                String recommendation =
                        getRecommendation(riskLevel, requiredClasses);

                alerts.add(new ShortageAlert(
                        student.getRollNumber(),
                        student.getName(),
                        subject.getSubjectName(),
                        attended,
                        total,
                        percentage,
                        requiredClasses,
                        riskLevel,
                        riskScore,
                        recommendation
                ));
            }
        }

        alerts.sort(
                Comparator.comparingDouble(
                        ShortageAlert::getPercentage
                )
        );

        return alerts;
    }

    // ONLY STUDENTS BELOW 75%
    public ArrayList<ShortageAlert> getShortageAlerts() {

        ArrayList<ShortageAlert> shortages = new ArrayList<>();

        for (ShortageAlert alert : getAllRiskRecords()) {

            if (alert.getPercentage() < REQUIRED_PERCENTAGE) {
                shortages.add(alert);
            }
        }

        // RECORD REPORT GENERATION
        auditService.logAction(
                "SHORTAGE REPORT GENERATED",
                "SHORTAGE",
                "Shortage records found: " + shortages.size()
        );

        return shortages;
    }
}