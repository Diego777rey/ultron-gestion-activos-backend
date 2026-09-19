package com.dev.ultron.dto.reportes;

/**
 * Cabeceras HTTP del paquete de reportes.
 * El visor del frontend las lee para titular la pestaña sin acoplarse al tipo.
 */
public final class ReporteHttpHeaders {

    public static final String TITULO = "X-Report-Title";

    private ReporteHttpHeaders() {
    }
}
