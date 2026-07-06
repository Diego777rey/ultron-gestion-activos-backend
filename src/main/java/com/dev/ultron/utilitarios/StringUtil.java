package com.dev.ultron.utilitarios;

public class StringUtil {

    private StringUtil() {
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String toUpperCase(String str) {
        return str != null ? str.toUpperCase() : null;
    }

    public static String normalizeUsername(String str) {
        return str != null ? str.trim().toUpperCase() : null;
    }

    public static String toLowerCase(String str) {
        return str != null ? str.toLowerCase() : null;
    }

    public static String capitalize(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
