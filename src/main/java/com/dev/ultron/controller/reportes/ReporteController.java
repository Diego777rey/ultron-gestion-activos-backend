package com.dev.ultron.controller.reportes;

import com.dev.ultron.service.reportes.ReporteGenericoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteGenericoService reporteGenericoService;

    @GetMapping("/{tipo}")
    public ResponseEntity<byte[]> generar(
            @PathVariable String tipo,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Long id
    ) {
        byte[] pdf = reporteGenericoService.generar(tipo, filtro, id);
        String filename = reporteGenericoService.nombreArchivo(tipo, id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
