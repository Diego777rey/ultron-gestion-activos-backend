package com.dev.ultron.generic;

/**
 * Normaliza valores de entrada antes de consultas y búsquedas en BD.
 * Alineado con las reglas de persistencia: textos e identificadores en MAYÚSCULAS, email en minúsculas.
 */
public final class SearchNormalizer {

    private SearchNormalizer() {
    }

    /**
     * Normaliza identificadores y textos de búsqueda (documento, chapa, ruc, username, filtros LIKE).
     * Retorna {@code null} si el valor es nulo o queda vacío tras trim.
     */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase();
    }

    /**
     * Documento comparable: sin puntos ni espacios, y sin el dígito verificador.
     * 6.124.099, 6124099 y 6124099-5 se buscan como 6124099.
     */
    public static String documentoBase(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return normalized.replaceAll("[\\s.]", "").replaceFirst("-\\d$", "");
    }

    /**
     * Normaliza emails para búsqueda exacta.
     * Retorna {@code null} si el valor es nulo o queda vacío tras trim.
     */
    public static String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }

    /**
     * Alias semántico para filtros paginados de listados.
     */
    public static String normalizeFilter(String filter) {
        return normalize(filter);
    }
}
