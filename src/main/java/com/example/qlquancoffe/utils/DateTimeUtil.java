package com.example.qlquancoffe.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility format ngày tháng
 */
public class DateTimeUtil {

    // Formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Format LocalDate sang String (dd/MM/yyyy)
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Format LocalDateTime sang String (dd/MM/yyyy HH:mm)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    /**
     * Format giờ (HH:mm)
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "";
    }

    /**
     * Parse String sang LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse String sang LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tính số ngày giữa 2 ngày
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Kiểm tra ngày có hợp lệ không (không quá khứ)
     */
    public static boolean isDateValid(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Lấy ngày đầu tuần
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    /**
     * Lấy ngày cuối tuần
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        return getStartOfWeek(date).plusDays(6);
    }

    /**
     * Format tương đối (vd: "2 giờ trước", "5 phút trước")
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());

        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";

        long hours = ChronoUnit.HOURS.between(dateTime, LocalDateTime.now());
        if (hours < 24) return hours + " giờ trước";

        long days = ChronoUnit.DAYS.between(dateTime, LocalDateTime.now());
        if (days < 7) return days + " ngày trước";

        return formatDateTime(dateTime);
    }
}