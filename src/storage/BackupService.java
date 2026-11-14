package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BackupService {

    private final Path dataDirectory =
            Paths.get("data");

    private final Path backupDirectory =
            Paths.get("backups");

    // ==========================================
    // 1. CREATE BACKUP
    // ==========================================

    public String createBackup() {

        try {

            if (!Files.exists(dataDirectory)) {
                return "Data directory does not exist.";
            }

            Files.createDirectories(backupDirectory);

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "yyyyMMdd_HHmmss_SSS"
                    )
            );

            Path backupFolder =
                    backupDirectory.resolve("backup_" + timestamp);

            Files.createDirectories(backupFolder);

            int copiedFiles = 0;

            try (Stream<Path> files =
                    Files.list(dataDirectory)) {

                for (Path file : files.toList()) {

                    if (Files.isRegularFile(file)) {

                        Files.copy(
                                file,
                                backupFolder.resolve(file.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING
                        );

                        copiedFiles++;
                    }
                }
            }

            return "Backup completed successfully. Files copied: "
                    + copiedFiles;

        } catch (IOException exception) {

            return "Backup failed: " + exception.getMessage();
        }
    }

    // ==========================================
    // 2. GET BACKUP HISTORY
    // ==========================================

    public List<Path> getBackupHistory() {

        List<Path> history = new ArrayList<>();

        if (!Files.exists(backupDirectory)) {
            return history;
        }

        try (Stream<Path> backups =
                Files.list(backupDirectory)) {

            backups
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName()
                            .toString().startsWith("backup_"))
                    .forEach(history::add);

            history.sort(
                    Comparator.comparing(
                            (Path path) ->
                                    path.getFileName().toString()
                    ).reversed()
            );

        } catch (IOException exception) {

            System.out.println(
                    "Unable to load backup history: "
                    + exception.getMessage()
            );
        }

        return history;
    }

    // ==========================================
    // 3. RESTORE SELECTED BACKUP
    // ==========================================

    public String restoreBackup(Path selectedBackup) {

        try {

            if (selectedBackup == null
                    || !Files.isDirectory(selectedBackup)) {

                return "Selected backup does not exist.";
            }

            Path realBackupDirectory =
                    backupDirectory.toAbsolutePath().normalize();

            Path realSelectedBackup =
                    selectedBackup.toAbsolutePath().normalize();

            if (!realSelectedBackup.startsWith(realBackupDirectory)
                    || realSelectedBackup.equals(realBackupDirectory)) {

                return "Invalid backup location.";
            }

            Files.createDirectories(dataDirectory);

            int restoredFiles = 0;

            try (Stream<Path> files =
                    Files.list(realSelectedBackup)) {

                for (Path file : files.toList()) {

                    if (Files.isRegularFile(file)) {

                        Files.copy(
                                file,
                                dataDirectory.resolve(file.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING
                        );

                        restoredFiles++;
                    }
                }
            }

            return "Recovery completed successfully. Files restored: "
                    + restoredFiles;

        } catch (IOException exception) {

            return "Recovery failed: " + exception.getMessage();
        }
    }

    // ==========================================
    // 4. RESTORE LATEST BACKUP
    // ==========================================

    public String restoreLatestBackup() {

        List<Path> history = getBackupHistory();

        if (history.isEmpty()) {
            return "No available backup found.";
        }

        return restoreBackup(history.get(0));
    }

    // ==========================================
    // 5. GET BACKUP COUNT
    // ==========================================

    public int getBackupCount() {

        return getBackupHistory().size();
    }

    // ==========================================
    // 6. GET FILE COUNT
    // ==========================================

    public int getFileCount(Path backupFolder) {

        if (backupFolder == null
                || !Files.isDirectory(backupFolder)) {

            return 0;
        }

        try (Stream<Path> files =
                Files.list(backupFolder)) {

            return (int) files
                    .filter(Files::isRegularFile)
                    .count();

        } catch (IOException exception) {

            return 0;
        }
    }
}