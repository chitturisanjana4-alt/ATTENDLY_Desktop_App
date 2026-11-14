package service;

import java.util.ArrayList;
import java.util.Comparator;

import model.Subject;
import storage.SubjectFileManager;

public class SubjectService {

    private ArrayList<Subject> subjects;
    private final AuditLogService auditService = new AuditLogService();

    public SubjectService() {

        subjects = SubjectFileManager.loadSubjects();

        if (subjects == null) {
            subjects = new ArrayList<>();
        }
    }

    // ADD SUBJECT
    public boolean addSubject(Subject subject) {

        if (subject == null) {
            return false;
        }

        if (subject.getSubjectCode() == null ||
            subject.getSubjectCode().trim().isEmpty() ||
            subject.getSubjectName() == null ||
            subject.getSubjectName().trim().isEmpty() ||
            subject.getFacultyName() == null ||
            subject.getFacultyName().trim().isEmpty() ||
            subject.getTotalClasses() < 0) {

            return false;
        }

        if (searchSubject(subject.getSubjectCode()) != null) {
            return false;
        }

        subjects.add(subject);
        saveData();

        auditService.logAction(
            "SUBJECT ADDED",
            "SUBJECT",
            "Subject Code: " + subject.getSubjectCode()
            + ", Subject Name: " + subject.getSubjectName()
            + ", Faculty: " + subject.getFacultyName()
        );

        return true;
    }

    // GET ALL SUBJECTS
    public ArrayList<Subject> getAllSubjects() {
        return new ArrayList<>(subjects);
    }

    // SEARCH USING SUBJECT CODE
    public Subject searchSubject(String code) {

        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (Subject subject : subjects) {

            if (subject.getSubjectCode()
                    .equalsIgnoreCase(code.trim())) {

                return subject;
            }
        }

        return null;
    }

    // SEARCH USING CODE, NAME, FACULTY OR TOTAL CLASSES
    public ArrayList<Subject> searchSubjects(String keyword) {

        ArrayList<Subject> result = new ArrayList<>();

        if (keyword == null) {
            return result;
        }

        keyword = keyword.trim().toLowerCase();

        for (Subject subject : subjects) {

            if (subject.getSubjectCode().toLowerCase().contains(keyword)
                || subject.getSubjectName().toLowerCase().contains(keyword)
                || subject.getFacultyName().toLowerCase().contains(keyword)
                || String.valueOf(subject.getTotalClasses()).contains(keyword)) {

                result.add(subject);
            }
        }

        return result;
    }

    // UPDATE SUBJECT
    public boolean updateSubject(
            String subjectCode,
            String subjectName,
            String facultyName,
            int totalClasses) {

        Subject subject = searchSubject(subjectCode);

        if (subject == null ||
            subjectName == null ||
            subjectName.trim().isEmpty() ||
            facultyName == null ||
            facultyName.trim().isEmpty() ||
            totalClasses < 0) {

            return false;
        }

        String oldName = subject.getSubjectName();

        subject.setSubjectName(subjectName.trim());
        subject.setFacultyName(facultyName.trim());
        subject.setTotalClasses(totalClasses);

        saveData();

        auditService.logAction(
            "SUBJECT UPDATED",
            "SUBJECT",
            "Subject Code: " + subjectCode
            + ", Previous Name: " + oldName
            + ", New Name: " + subjectName.trim()
        );

        return true;
    }

    // DELETE SUBJECT
    public boolean deleteSubject(String code) {

        Subject subject = searchSubject(code);

        if (subject == null) {
            return false;
        }

        String subjectName = subject.getSubjectName();

        subjects.remove(subject);
        saveData();

        auditService.logAction(
            "SUBJECT DELETED",
            "SUBJECT",
            "Subject Code: " + code
            + ", Subject Name: " + subjectName
        );

        return true;
    }

    // SORT BY SUBJECT CODE
    public ArrayList<Subject> sortByCode() {

        ArrayList<Subject> sorted = new ArrayList<>(subjects);

        sorted.sort(
            Comparator.comparing(
                Subject::getSubjectCode,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return sorted;
    }

    // SORT BY SUBJECT NAME
    public ArrayList<Subject> sortByName() {

        ArrayList<Subject> sorted = new ArrayList<>(subjects);

        sorted.sort(
            Comparator.comparing(
                Subject::getSubjectName,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return sorted;
    }

    // SORT BY TOTAL CLASSES
    public ArrayList<Subject> sortByClasses() {

        ArrayList<Subject> sorted = new ArrayList<>(subjects);

        sorted.sort(
            Comparator.comparingInt(Subject::getTotalClasses)
        );

        return sorted;
    }

    // TOTAL SUBJECTS
    public int getTotalSubjects() {
        return subjects.size();
    }

    // SAVE DATA
    private void saveData() {
        SubjectFileManager.saveSubjects(subjects);
    }
}