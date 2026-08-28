package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.output.ReporteFila;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JasperReportServiceTest {

    @Test
    void compilaYExportaPlantillaGenerica() {
        JasperReportService service = new JasperReportService();
        List<ReporteFila> filas = List.of(
                ReporteFila.builder()
                        .numero(1)
                        .codigo("ABC-001")
                        .nombre("FILTRO ACEITE")
                        .descripcion("Uso general")
                        .categoria("REPUESTOS")
                        .subcategoria("MOTOR")
                        .precio("150.000 Gs")
                        .extra("12")
                        .estado("ACTIVO")
                        .build()
        );

        byte[] pdf = service.exportarInventario(
                Map.of(
                        "TITULO", "Catálogo de productos",
                        "SUBTITULO", "Listado genérico de inventario",
                        "SISTEMA", "Ultron System",
                        "FECHA_GENERACION", "28/08/2026 19:00:00",
                        "COLUMNA_EXTRA", "STOCK",
                        "CANTIDAD", filas.size(),
                        "FILTRO", ""
                ),
                filas
        );

        assertThat(pdf).isNotEmpty();
        assertThat(pdf).startsWith("%PDF".getBytes());
    }
}
