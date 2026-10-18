package com.dev.ultron.controller.reportes;

import com.dev.ultron.dto.reportes.FormatoReporte;
import com.dev.ultron.dto.reportes.ReporteHttpHeaders;
import com.dev.ultron.service.reportes.ReporteGenericoService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteGenericoService reporteGenericoService;

    public ReporteController(ReporteGenericoService reporteGenericoService) {
        this.reporteGenericoService = reporteGenericoService;
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<byte[]> generar(
            @PathVariable String tipo,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long idEmpresa,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false, defaultValue = "pdf") String formato
    ) {
        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        byte[] contenido = reporteGenericoService.generar(
                tipo, filtro, id, idEmpresa, fechaDesde, fechaHasta, formatoReporte);
        String filename = reporteGenericoService.nombreArchivo(tipo, id, formatoReporte);
        String titulo = encodeHeader(reporteGenericoService.titulo(tipo));
        ContentDisposition disposition = (formatoReporte == FormatoReporte.EXCEL
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        MediaType mediaType = formatoReporte == FormatoReporte.EXCEL
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(ReporteHttpHeaders.TITULO, titulo)
                .contentType(mediaType)
                .contentLength(contenido.length)
                .body(contenido);
    }

    private static String encodeHeader(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
