package com.dev.ultron.service.reportes;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor genérico de JasperReports. Compila plantillas JRXML (Jaspersoft Studio)
 * y las rellena con cualquier colección de beans.
 */
@Service
public class JasperReportService {

    public static final String PLANTILLA_INVENTARIO = "reports/inventario_generico.jrxml";
    public static final String PLANTILLA_ORDEN_DETALLE = "reports/orden_trabajo_detalle.jrxml";
    public static final String PLANTILLA_TRANSFERENCIA_DETALLE = "reports/transferencia_detalle.jrxml";
    public static final String LOGO_CLASSPATH = "reports/chservice.png";

    private static final Logger log = LoggerFactory.getLogger(JasperReportService.class);

    private final ConcurrentHashMap<String, JasperReport> compilados = new ConcurrentHashMap<>();
    private final byte[] logoBytes = cargarLogo();

    @PostConstruct
    void precargarPlantillas() {
        long inicio = System.nanoTime();
        for (String plantilla : List.of(PLANTILLA_INVENTARIO, PLANTILLA_ORDEN_DETALLE, PLANTILLA_TRANSFERENCIA_DETALLE)) {
            try {
                compilados.computeIfAbsent(plantilla, this::compilar);
            } catch (RuntimeException ex) {
                log.warn("No se pudo precargar la plantilla {}", plantilla, ex);
            }
        }
        try {
            exportarInventario(parametrosCalentamiento(), List.of());
        } catch (RuntimeException ex) {
            log.warn("No se pudo calentar el motor PDF", ex);
        }
        log.info("Plantillas de reporte listas en {} ms", (System.nanoTime() - inicio) / 1_000_000);
    }

    public byte[] exportarPdf(String plantillaClasspath, Map<String, Object> parametros, Collection<?> filas) {
        try {
            return JasperExportManager.exportReportToPdf(rellenar(plantillaClasspath, parametros, filas));
        } catch (JRException ex) {
            throw new IllegalStateException("No se pudo generar el reporte PDF.", ex);
        }
    }

    JasperPrint rellenar(String plantillaClasspath, Map<String, Object> parametros, Collection<?> filas) {
        try {
            JasperReport report = compilados.computeIfAbsent(plantillaClasspath, this::compilar);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(filas);
            Map<String, Object> fillParams = parametros == null
                    ? new HashMap<>()
                    : new HashMap<>(parametros);
            completarIdentidad(fillParams);
            return JasperFillManager.fillReport(report, fillParams, dataSource);
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

    private void completarIdentidad(Map<String, Object> parametros) {
        if (logoBytes.length > 0) {
            parametros.putIfAbsent("LOGO", new ByteArrayInputStream(logoBytes));
        }
        parametros.putIfAbsent("USUARIO", usuarioActual());
    }

    private static byte[] cargarLogo() {
        ClassPathResource resource = new ClassPathResource(LOGO_CLASSPATH);
        if (!resource.exists()) {
            log.warn("No se encontró el logo de reportes en {}", LOGO_CLASSPATH);
            return new byte[0];
        }
        try (InputStream input = resource.getInputStream()) {
            return input.readAllBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el logo de los reportes.", ex);
        }
    }

    private static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "";
        }
        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return "";
        }
        return name;
    }

    private static Map<String, Object> parametrosCalentamiento() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", "");
        parametros.put("SUBTITULO", "");
        parametros.put("SISTEMA", "");
        parametros.put("FECHA_GENERACION", "");
        parametros.put("COLUMNA_EXTRA", "");
        parametros.put("CANTIDAD", 0);
        parametros.put("FILTRO", "");
        return parametros;
    }
}
