package com.example.qlquancoffe.utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility validate input
 */
public class ValidationUtil {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^0[0-9]{9}$"
    );

    // ==================== VALIDATION METHODS ====================

    /**
     * Kiểm tra chuỗi rỗng
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Kiểm tra email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kiểm tra số điện thoại Việt Nam (10 số, bắt đầu bằng 0)
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Kiểm tra số tiền hợp lệ (> 0)
     */
    public static boolean isValidMoney(String moneyStr) {
        try {
            BigDecimal amount = new BigDecimal(moneyStr);
            return amount.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Kiểm tra số nguyên dương
     */
    public static boolean isPositiveInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Kiểm tra username (6-20 ký tự, chữ + số)
     */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        return username.matches("^[a-zA-Z0-9]{6,20}$");
    }

    /**
     * Lấy số tiền từ chuỗi đã format (VD: "25,000 ₫" -> 25000)
     */
    public static BigDecimal parseFormattedMoney(String formatted) {
        try {
            String cleaned = formatted.replaceAll("[^0-9]", "");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Kiểm tra độ dài chuỗi
     */
    public static boolean isLengthValid(String value, int min, int max) {
        if (isEmpty(value)) return false;
        int len = value.trim().length();
        return len >= min && len <= max;
    }
}