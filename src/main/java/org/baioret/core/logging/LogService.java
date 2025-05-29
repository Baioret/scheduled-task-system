package org.baioret.core.logging;

import java.util.logging.Logger;

public class LogService {
    public static Logger logger = null;
    static {
        try {
            logger = Logger.getLogger(LogService.class.getName());
            DailyLogManager.setupLogger();
        } catch (Exception e) {
            System.err.println("Could not setup logger configuration: " + e);
        }
    }
}
