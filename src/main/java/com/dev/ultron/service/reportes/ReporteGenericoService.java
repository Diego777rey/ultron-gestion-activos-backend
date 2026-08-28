package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.DateUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orquesta el reporte genérico de inventario: resuelve la fuente según el tipo
 * (producto o servicio) y delega la generación PDF al motor Jasper.
 */
@Service
public class ReporteGenericoService {

    private final JasperReportService jasperReportService;
    private final Map<TipoReporte, ReporteFuente> fuentes;

    public ReporteGenericoService(JasperReportService jasperReportService, List<ReporteFuente> fuentes) {
        this.jasperReportService = jasperReportService;
        this.fuentes = new HashMap<>();
        for (ReporteFuente fuente : fuentes) {
            this.fuentes.put(fuente.tipo(), fuente);
        }
    }

    public byte[] generar(String tipoRaw, String filtro, Long id) {
        ReporteFuente fuente = resolver(tipoRaw);
        List<ReporteFila> filas = id != null ? fuente.buscarPorId(id) : fuente.listar(filtro);
        Map<String, Object> parametros = construirParametros(fuente, filtro, filas.size());
        return jasperReportService.exportarInventario(parametros, filas);
    }

    public String nombreArchivo(String tipoRaw, Long id) {
        ReporteFuente fuente = resolver(tipoRaw);
        if (id != null) {
            return fuente.nombreArchivo().replace(".pdf", "-" + id + ".pdf");
        }
        return fuente.nombreArchivo();
    }

    private Map<String, Object> construirParametros(ReporteFuente fuente, String filtro, int cantidad) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", fuente.titulo());
        parametros.put("SUBTITULO", fuente.subtitulo());
        parametros.put("SISTEMA", AppConstants.SISTEMA_NOMBRE);
        parametros.put("FECHA_GENERACION", DateUtil.format(DateUtil.nowDateTime()));
        parametros.put("COLUMNA_EXTRA", fuente.etiquetaColumnaExtra());
        parametros.put("CANTIDAD", cantidad);
        parametros.put("FILTRO", filtro == null ? "" : filtro.trim().toUpperCase(Locale.ROOT));
        return parametros;
    }

    private ReporteFuente resolver(String tipoRaw) {
        TipoReporte tipo = TipoReporte.from(tipoRaw);
        ReporteFuente fuente = fuentes.get(tipo);
        if (fuente == null) {
            throw new IllegalArgumentException("No hay una fuente de reporte registrada para: " + tipo);
        }
        return fuente;
    }
}
