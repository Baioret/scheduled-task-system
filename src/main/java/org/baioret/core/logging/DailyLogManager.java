package org.baioret.core.logging;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class DailyLogManager {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static FileHandler currentFileHandler;
    private static String currentDate;

    public static void setupLogger() throws IOException {
        LogService.logger.setUseParentHandlers(false);
        updateLogger();
        startDateCheckThread();
    }

    private static synchronized void updateLogger() throws IOException {
        String today = dateFormat.format(Instant.now());

        if (today.equals(currentDate)) {
            return;
        }

        if (currentFileHandler != null) {
            currentFileHandler.close();
            LogService.logger.removeHandler(currentFileHandler);
        }

        String fileName = "logs/app-" + today + ".log";
        currentFileHandler = new FileHandler(fileName, 1024 * 1024, 5, true);
        currentFileHandler.setFormatter(new SimpleFormatter());
        LogService.logger.addHandler(currentFileHandler);

        currentDate = today;
        LogService.logger.info("Logger switched to file " + fileName);
    }

    private static void startDateCheckThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    updateLogger();
                } catch (Exception e) {
                    LogService.logger.severe("Logs rotation error. " + e.getMessage());
                }
            }
        }).start();
    }
}
