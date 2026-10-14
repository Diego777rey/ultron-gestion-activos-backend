package com.dev.ultron.dto.reportes;

import java.util.Locale;

/**
 * Formato de salida del paquete de reportes REST.
 */
public enum FormatoReporte {
    PDF,
    EXCEL;

    public static FormatoReporte from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PDF;
        }
        String normalizado = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case "pdf" -> PDF;
            case "excel", "xlsx" -> EXCEL;
            default -> throw new IllegalArgumentException("Formato de reporte no soportado: " + raw);
        };
    }
}
