package com.dev.ultron.dto.reportes;

import java.util.Locale;

/**
 * Tipos de catálogo que puede emitir el reporte genérico de inventario.
 */
public enum TipoReporte {
    PRODUCTO,
    SERVICIO,
    CLIENTE,
    FUNCIONARIO,
    USUARIO,
    TRANSFERENCIA,
    SOLICITUD_REPUESTO,
    ORDEN_TRABAJO,
    HISTORIAL,
    VEHICULO;

    public static TipoReporte from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el tipo de reporte.");
        }
        try {
            return TipoReporte.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de reporte no soportado: " + raw);
        }
    }
}
