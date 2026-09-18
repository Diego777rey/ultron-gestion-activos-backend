package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.TipoReporte;

import java.util.List;

/**
 * Fuente de datos intercambiable de reportes PDF.
 * Cada implementación adapta un dominio a los beans de su plantilla Jasper.
 */
public interface ReporteFuente {

    TipoReporte tipo();

    String titulo();

    String subtitulo();

    String etiquetaColumnaExtra();

    String nombreArchivo();

    List<?> listar(String filtro);

    List<?> buscarPorId(Long id);

    default String plantilla() {
        return JasperReportService.PLANTILLA_INVENTARIO;
    }
}
