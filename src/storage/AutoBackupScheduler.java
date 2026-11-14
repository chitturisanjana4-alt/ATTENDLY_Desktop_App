package storage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoBackupScheduler {

    private final BackupService backupService =
            new BackupService();

    private ScheduledExecutorService scheduler;

    private boolean running = false;

    // ==========================================
    // 1. START AUTOMATIC BACKUP
    // ==========================================

    public synchronized String startAutoBackup(
            long intervalMinutes) {

        if (intervalMinutes <= 0) {
            return "Backup interval must be greater than zero.";
        }

        if (running) {
            return "Automatic backup is already running.";
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(
                runnable -> {

                    Thread thread = new Thread(
                            runnable,
                            "ATTENDLY-AutoBackup"
                    );

                    thread.setDaemon(true);

                    return thread;
                }
        );

        scheduler.scheduleAtFixedRate(
                () -> {

                    try {

                        String result =
                                backupService.createBackup();

                        System.out.println(
                                "[ATTENDLY AUTO BACKUP] " + result
                        );

                    } catch (Exception exception) {

                        System.out.println(
                                "[ATTENDLY AUTO BACKUP ERROR] "
                                + exception.getMessage()
                        );
                    }

                },
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES
        );

        running = true;

        return "Automatic backup started. Interval: "
                + intervalMinutes + " minutes.";
    }

    // ==========================================
    // 2. STOP AUTOMATIC BACKUP
    // ==========================================

    public synchronized String stopAutoBackup() {

        if (!running || scheduler == null) {
            return "Automatic backup is not running.";
        }

        scheduler.shutdownNow();

        scheduler = null;
        running = false;

        return "Automatic backup stopped.";
    }

    // ==========================================
    // 3. CHECK BACKUP STATUS
    // ==========================================

    public synchronized boolean isRunning() {
        return running;
    }

    // ==========================================
    // 4. CLOSE SCHEDULER
    // ==========================================

    public synchronized void shutdown() {

        if (scheduler != null) {

            scheduler.shutdownNow();

            scheduler = null;
            running = false;
        }
    }
}