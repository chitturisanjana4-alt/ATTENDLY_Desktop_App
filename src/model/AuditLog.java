package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {

    private String action;
    private String category;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog(
            String action,
            String category,
            String details,
            LocalDateTime timestamp) {

        this.action = action;
        this.category = category;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getCategory() {
        return category;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"
                );

        return timestamp.format(formatter);
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}