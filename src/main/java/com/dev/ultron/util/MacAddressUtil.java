package com.dev.ultron.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza y valida direcciones MAC (AA:BB:CC:DD:EE:FF) para vínculo caja-terminal.
 */
public final class MacAddressUtil {

    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-F]{2}:){5}[0-9A-F]{2}$");

    private MacAddressUtil() {
    }

    /**
     * Normaliza a mayúsculas con separador {@code :}. Acepta {@code :}, {@code -} o sin separador.
     * Devuelve {@code null} si el valor es vacío.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String hex = trimmed.replace("-", "").replace(":", "").replace(".", "");
        hex = hex.toUpperCase(Locale.ROOT);
        if (!hex.matches("[0-9A-F]{12}")) {
            throw new IllegalArgumentException(
                    "Dirección MAC inválida. Usá el formato AA:BB:CC:DD:EE:FF");
        }
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 12; i += 2) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(hex, i, i + 2);
        }
        return sb.toString();
    }

    public static boolean isValidNormalized(String mac) {
        return mac != null && MAC_PATTERN.matcher(mac).matches();
    }

    public static boolean equalsNormalized(String a, String b) {
        String na = safeNormalize(a);
        String nb = safeNormalize(b);
        if (na == null || nb == null) {
            return false;
        }
        return na.equals(nb);
    }

    /** Como {@link #normalize} pero sin lanzar: vacío o inválido → {@code null}. */
    public static String safeNormalize(String raw) {
        try {
            return normalize(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
