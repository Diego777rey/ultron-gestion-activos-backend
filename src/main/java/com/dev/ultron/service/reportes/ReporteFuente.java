package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.output.ReporteFila;

import java.util.List;

/**
 * Fuente de datos intercambiable del reporte genérico de inventario.
 * Cada implementación adapta un producto o servicio (u otro catálogo futuro)
 * a {@link ReporteFila} sin acoplar el motor Jasper al dominio.
 */
public interface ReporteFuente {

    TipoReporte tipo();

    String titulo();

    String subtitulo();

    String etiquetaColumnaExtra();

    String nombreArchivo();

    List<ReporteFila> listar(String filtro);

    List<ReporteFila> buscarPorId(Long id);
}
