package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import model.AuditLog;
import storage.AuditLogFileManager;

public class AuditLogService {

    public void logAction(
            String action,
            String category,
            String details) {

        if (action == null || action.isBlank()
                || category == null || category.isBlank()) {

            System.err.println(
                    "Audit action and category are required."
            );

            return;
        }

        AuditLog log = new AuditLog(
                action.trim(),
                category.trim(),
                details == null ? "" : details.trim(),
                LocalDateTime.now()
        );

        AuditLogFileManager.saveLog(log);
    }

    public ArrayList<AuditLog> getAllLogs() {

        ArrayList<AuditLog> logs =
                AuditLogFileManager.loadLogs();

        logs.sort(
                Comparator.comparing(
                        AuditLog::getTimestamp
                ).reversed()
        );

        return logs;
    }

    public ArrayList<AuditLog> searchLogs(String keyword) {

        ArrayList<AuditLog> results = getAllLogs();

        if (keyword == null || keyword.isBlank()) {
            return results;
        }

        String search = keyword.trim().toLowerCase();

        return results.stream()
                .filter(log ->
                        contains(log.getAction(), search)
                        || contains(log.getCategory(), search)
                        || contains(log.getDetails(), search)
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<AuditLog> filterByCategory(
            String category) {

        ArrayList<AuditLog> results = getAllLogs();

        if (category == null
                || category.equalsIgnoreCase("ALL")) {

            return results;
        }

        return results.stream()
                .filter(log ->
                        log.getCategory().equalsIgnoreCase(category)
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<AuditLog> getLogsForDate(
            LocalDate date) {

        ArrayList<AuditLog> results = getAllLogs();

        if (date == null) {
            return results;
        }

        return results.stream()
                .filter(log ->
                        log.getTimestamp().toLocalDate().equals(date)
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int getTotalLogs() {
        return getAllLogs().size();
    }

    public long getTodayLogCount() {

        LocalDate today = LocalDate.now();

        return getAllLogs().stream()
                .filter(log ->
                        log.getTimestamp()
                                .toLocalDate()
                                .equals(today)
                )
                .count();
    }

    public long getCategoryCount(String category) {

        if (category == null) {
            return 0;
        }

        return getAllLogs().stream()
                .filter(log ->
                        log.getCategory()
                                .equalsIgnoreCase(category)
                )
                .count();
    }

    public ArrayList<String> getAvailableCategories() {

        return getAllLogs().stream()
                .map(AuditLog::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean contains(
            String value,
            String search) {

        return value != null
                && value.toLowerCase().contains(search);
    }
}