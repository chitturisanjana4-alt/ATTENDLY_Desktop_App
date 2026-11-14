package service;

import java.util.ArrayList;
import java.time.LocalDate;

import model.Attendance;
import storage.AttendanceFileManager;

public class AttendanceService {

    private ArrayList<Attendance> records;
    private final AuditLogService auditService = new AuditLogService();

    public AttendanceService() {

        records = AttendanceFileManager.loadAttendance();

        if (records == null) {
            records = new ArrayList<>();
        }
    }

    // MARK ATTENDANCE
    public boolean markAttendance(String rollNumber,
                                  String subjectCode,
                                  LocalDate date,
                                  boolean present) {

        if (rollNumber == null || rollNumber.trim().isEmpty()
                || subjectCode == null || subjectCode.trim().isEmpty()
                || date == null) {
            return false;
        }

        // PREVENT DUPLICATE ATTENDANCE
        for (Attendance record : records) {

            if (record.getRollNumber().equalsIgnoreCase(rollNumber)
                    && record.getSubjectCode().equalsIgnoreCase(subjectCode)
                    && record.getDate().equals(date)) {

                return false;
            }
        }

        Attendance attendance = new Attendance(
                rollNumber, subjectCode, date, present);

        records.add(attendance);

        AttendanceFileManager.saveAttendance(records);

        // AUDIT LOG
        auditService.logAction(
                "ATTENDANCE MARKED",
                "ATTENDANCE",
                "Roll Number: " + rollNumber
                + ", Subject Code: " + subjectCode
                + ", Date: " + date
                + ", Status: " + (present ? "PRESENT" : "ABSENT")
        );

        return true;
    }

    // GET ALL ATTENDANCE RECORDS
    public ArrayList<Attendance> getAllAttendance() {
        return new ArrayList<>(records);
    }

    // CALCULATE TOTAL CLASSES
    public int getTotalClasses(String rollNumber, String subjectCode) {

        int total = 0;

        for (Attendance record : records) {

            if (record.getRollNumber().equalsIgnoreCase(rollNumber)
                    && record.getSubjectCode().equalsIgnoreCase(subjectCode)) {

                total++;
            }
        }

        return total;
    }

    // CALCULATE ATTENDED CLASSES
    public int getAttendedClasses(String rollNumber, String subjectCode) {

        int attended = 0;

        for (Attendance record : records) {

            if (record.getRollNumber().equalsIgnoreCase(rollNumber)
                    && record.getSubjectCode().equalsIgnoreCase(subjectCode)
                    && record.isPresent()) {

                attended++;
            }
        }

        return attended;
    }

    // CALCULATE ATTENDANCE PERCENTAGE
    public double calculatePercentage(String rollNumber,
                                      String subjectCode) {

        int total = getTotalClasses(rollNumber, subjectCode);

        if (total == 0) {
            return 0.0;
        }

        int attended = getAttendedClasses(rollNumber, subjectCode);

        return (attended * 100.0) / total;
    }
}