package org.baioret.integrationtest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EuropeanDateFormatter {
    private final static String europeanDatePattern = "dd.MM.yyyy HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(europeanDatePattern);

    public static String getFromLocalDateTime(LocalDateTime localDateTime) {
        return formatter.format(localDateTime);
    }
}
