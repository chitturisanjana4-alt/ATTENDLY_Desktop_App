package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;

import model.AuditLog;

public class AuditLogFileManager {

    private static final Path FILE_PATH =
            Paths.get("data", "audit_logs.txt");

    public static void saveLog(AuditLog log) {

        try {

            Files.createDirectories(FILE_PATH.getParent());

            try (BufferedWriter writer =
                    Files.newBufferedWriter(
                            FILE_PATH,
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND)) {

                writer.write(encode(log.getTimestamp().toString()));
                writer.write("|");
                writer.write(encode(log.getCategory()));
                writer.write("|");
                writer.write(encode(log.getAction()));
                writer.write("|");
                writer.write(encode(log.getDetails()));
                writer.newLine();
            }

        } catch (IOException exception) {

            System.err.println(
                    "Unable to save audit log: "
                    + exception.getMessage()
            );
        }
    }

    public static ArrayList<AuditLog> loadLogs() {

        ArrayList<AuditLog> logs = new ArrayList<>();

        if (!Files.exists(FILE_PATH)) {
            return logs;
        }

        try (BufferedReader reader =
                Files.newBufferedReader(
                        FILE_PATH,
                        StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|", 4);

                if (parts.length != 4) {
                    continue;
                }

                try {

                    LocalDateTime timestamp =
                            LocalDateTime.parse(
                                    decode(parts[0])
                            );

                    String category = decode(parts[1]);
                    String action = decode(parts[2]);
                    String details = decode(parts[3]);

                    logs.add(
                            new AuditLog(
                                    action,
                                    category,
                                    details,
                                    timestamp
                            )
                    );

                } catch (RuntimeException exception) {

                    System.err.println(
                            "Skipping invalid audit record."
                    );
                }
            }

        } catch (IOException exception) {

            System.err.println(
                    "Unable to load audit logs: "
                    + exception.getMessage()
            );
        }

        return logs;
    }

    private static String encode(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("%", "%25")
                .replace("|", "%7C")
                .replace("\r", "%0D")
                .replace("\n", "%0A");
    }

    private static String decode(String value) {

        return value
                .replace("%0A", "\n")
                .replace("%0D", "\r")
                .replace("%7C", "|")
                .replace("%25", "%");
    }
}