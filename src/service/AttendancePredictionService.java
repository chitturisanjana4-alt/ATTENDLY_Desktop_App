package service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.Attendance;
import model.Student;
import model.Subject;

public class AttendancePredictionService {

    private static final double REQUIRED_PERCENTAGE = 75.0;
    private static final int FORECAST_CLASSES = 5;

    private final StudentService studentService;
    private final SubjectService subjectService;
    private final AttendanceService attendanceService;

    public AttendancePredictionService() {
        studentService = new StudentService();
        subjectService = new SubjectService();
        attendanceService = new AttendanceService();
    }

    public static class AttendancePrediction {

        private final String rollNumber;
        private final String studentName;
        private final String subjectCode;
        private final String subjectName;

        private final int attendedClasses;
        private final int totalClasses;

        private final double currentPercentage;
        private final double predictedPercentage;

        private final String riskLevel;
        private final String recommendation;

        public AttendancePrediction(
                String rollNumber,
                String studentName,
                String subjectCode,
                String subjectName,
                int attendedClasses,
                int totalClasses,
                double currentPercentage,
                double predictedPercentage,
                String riskLevel,
                String recommendation) {

            this.rollNumber = rollNumber;
            this.studentName = studentName;
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
            this.attendedClasses = attendedClasses;
            this.totalClasses = totalClasses;
            this.currentPercentage = currentPercentage;
            this.predictedPercentage = predictedPercentage;
            this.riskLevel = riskLevel;
            this.recommendation = recommendation;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getSubjectCode() {
            return subjectCode;
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

        public double getCurrentPercentage() {
            return currentPercentage;
        }

        public double getPredictedPercentage() {
            return predictedPercentage;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public ArrayList<AttendancePrediction> getAllPredictions() {

        ArrayList<AttendancePrediction> predictions =
                new ArrayList<>();

        List<Student> students = studentService.getAllStudents();
        List<Subject> subjects = subjectService.getAllSubjects();
        List<Attendance> records = attendanceService.getAllAttendance();

        for (Student student : students) {

            for (Subject subject : subjects) {

                ArrayList<Attendance> studentRecords =
                        new ArrayList<>();

                for (Attendance attendance : records) {

                    if (attendance.getRollNumber().equalsIgnoreCase(
                            student.getRollNumber())
                            && attendance.getSubjectCode().equalsIgnoreCase(
                                    subject.getSubjectCode())) {

                        studentRecords.add(attendance);
                    }
                }

                int total = studentRecords.size();
                int attended = 0;

                for (Attendance attendance : studentRecords) {
                    if (attendance.isPresent()) {
                        attended++;
                    }
                }

                if (total == 0) {
                    continue;
                }

                double currentPercentage =
                        (attended * 100.0) / total;

                studentRecords.sort(
                        Comparator.comparing(Attendance::getDate)
                );

                int recentCount = Math.min(
                        FORECAST_CLASSES,
                        studentRecords.size()
                );

                int recentPresent = 0;

                for (int i = studentRecords.size() - recentCount;
                        i < studentRecords.size(); i++) {

                    if (studentRecords.get(i).isPresent()) {
                        recentPresent++;
                    }
                }

                double recentRate =
                        (recentPresent * 100.0) / recentCount;

                int futureAttended =
                        (int) Math.round(
                                (recentRate / 100.0)
                                * FORECAST_CLASSES
                        );

                double predictedPercentage =
                        ((attended + futureAttended) * 100.0)
                        / (total + FORECAST_CLASSES);

                String riskLevel;
                String recommendation;

                if (predictedPercentage < 50.0) {

                    riskLevel = "CRITICAL";

                    recommendation =
                            "Immediate attendance improvement required.";

                } else if (predictedPercentage < REQUIRED_PERCENTAGE) {

                    riskLevel = "WARNING";

                    recommendation =
                            "Attend upcoming classes regularly.";

                } else {

                    riskLevel = "SAFE";

                    recommendation =
                            "Maintain the current attendance pattern.";
                }

                AttendancePrediction prediction =
                        new AttendancePrediction(
                                student.getRollNumber(),
                                student.getName(),
                                subject.getSubjectCode(),
                                subject.getSubjectName(),
                                attended,
                                total,
                                currentPercentage,
                                predictedPercentage,
                                riskLevel,
                                recommendation
                        );

                predictions.add(prediction);
            }
        }

        predictions.sort(
                Comparator.comparingDouble(
                        AttendancePrediction::getPredictedPercentage
                )
        );

        return predictions;
    }

    public ArrayList<AttendancePrediction> getAtRiskPredictions() {

        ArrayList<AttendancePrediction> result =
                new ArrayList<>();

        for (AttendancePrediction prediction : getAllPredictions()) {

            if (prediction.getPredictedPercentage()
                    < REQUIRED_PERCENTAGE) {

                result.add(prediction);
            }
        }

        return result;
    }

    public int getCriticalCount() {

        int count = 0;

        for (AttendancePrediction prediction : getAllPredictions()) {

            if (prediction.getRiskLevel().equals("CRITICAL")) {
                count++;
            }
        }

        return count;
    }

    public int getWarningCount() {

        int count = 0;

        for (AttendancePrediction prediction : getAllPredictions()) {

            if (prediction.getRiskLevel().equals("WARNING")) {
                count++;
            }
        }

        return count;
    }
}