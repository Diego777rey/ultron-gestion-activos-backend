package com.dev.ultron.service.reportes;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor genérico de JasperReports. Compila plantillas JRXML (Jaspersoft Studio)
 * y las rellena con cualquier colección de beans.
 */
@Service
public class JasperReportService {

    static final String PLANTILLA_INVENTARIO = "reports/inventario_generico.jrxml";

    private final ConcurrentHashMap<String, JasperReport> compilados = new ConcurrentHashMap<>();

    public byte[] exportarPdf(String plantillaClasspath, Map<String, Object> parametros, Collection<?> filas) {
        try {
            JasperReport report = compilados.computeIfAbsent(plantillaClasspath, this::compilar);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(filas);
            Map<String, Object> fillParams = parametros == null
                    ? new HashMap<>()
                    : new HashMap<>(parametros);
            JasperPrint print = JasperFillManager.fillReport(report, fillParams, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException ex) {
            throw new IllegalStateException("No se pudo generar el reporte PDF.", ex);
        }
    }

    public byte[] exportarInventario(Map<String, Object> parametros, Collection<?> filas) {
        return exportarPdf(PLANTILLA_INVENTARIO, parametros, filas);
    }

    private JasperReport compilar(String plantillaClasspath) {
        ClassPathResource resource = new ClassPathResource(plantillaClasspath);
        if (!resource.exists()) {
            throw new IllegalStateException("No se encontró la plantilla de reporte: " + plantillaClasspath);
        }
        try (InputStream input = resource.getInputStream()) {
            return JasperCompileManager.compileReport(input);
        } catch (IOException | JRException ex) {
            throw new IllegalStateException("No se pudo compilar la plantilla JRXML: " + plantillaClasspath, ex);
        }
    }
}
