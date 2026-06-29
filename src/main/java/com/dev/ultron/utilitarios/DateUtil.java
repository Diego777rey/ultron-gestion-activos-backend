package com.dev.ultron.utilitarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private DateUtil() {
    }

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        if (dateStr.contains("-")) {
            return LocalDate.parse(dateStr);
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        if (dateTimeStr.contains("T") || dateTimeStr.contains("-")) {
            return LocalDateTime.parse(dateTimeStr);
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    public static LocalDate now() {
        return LocalDate.now();
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
}
