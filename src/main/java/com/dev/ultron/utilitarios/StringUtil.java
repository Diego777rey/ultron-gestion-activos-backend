package com.dev.ultron.utilitarios;

import com.dev.ultron.generic.SearchNormalizer;

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
        return SearchNormalizer.normalize(str);
    }

    public static String toLowerCase(String str) {
        return str != null ? str.toLowerCase() : null;
    }
}
