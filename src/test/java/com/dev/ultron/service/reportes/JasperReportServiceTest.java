package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.dto.reportes.output.ReporteOtDetalleFila;
import com.dev.ultron.dto.reportes.output.ReporteTransferenciaDetalleFila;
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

    @Test
    void compilaYExportaPlantillaDetalleOrden() {
        JasperReportService service = new JasperReportService();
        List<ReporteOtDetalleFila> filas = List.of(
                ReporteOtDetalleFila.builder()
                        .numeroOrden("OT-001")
                        .etapa("DIAGNOSTICO")
                        .fecha("18/09/2026 10:00:00")
                        .presupuesto("250.000 GS")
                        .encabezado("Cliente: Juan Pérez\nVehículo: Toyota Corolla\nFalla: Ruido en motor")
                        .tipoLinea("SERVICIO")
                        .nombreLinea("Diagnóstico general")
                        .cantidad("1")
                        .precio("250.000 GS")
                        .subtotal("250.000 GS")
                        .build()
        );

        byte[] pdf = service.exportarPdf(
                JasperReportService.PLANTILLA_ORDEN_DETALLE,
                Map.of(
                        "TITULO", "Detalle de órdenes de trabajo",
                        "SUBTITULO", "Vista completa",
                        "SISTEMA", "Ultron System",
                        "FECHA_GENERACION", "18/09/2026 19:00:00",
                        "COLUMNA_EXTRA", "",
                        "CANTIDAD", filas.size(),
                        "FILTRO", ""
                ),
                filas
        );

        assertThat(pdf).isNotEmpty();
        assertThat(pdf).startsWith("%PDF".getBytes());
    }

    @Test
    void compilaYExportaPlantillaDetalleTransferencia() {
        JasperReportService service = new JasperReportService();
        List<ReporteTransferenciaDetalleFila> filas = List.of(
                ReporteTransferenciaDetalleFila.builder()
                        .numero("TRF-20260920-0001")
                        .estado("Conferido")
                        .fecha("20/09/2026 11:00:00")
                        .encabezado("Sector origen: DEPOSITO\nSector destino: SALON DE VENTAS\nEntregado por: Admin")
                        .codigo("LIQ REFR ORG")
                        .nombreLinea("REFRIGERANTE ANTICONGELANTE")
                        .cantidad("7")
                        .estadoLinea("Verificado")
                        .firmaEntrega("Admin")
                        .firmaRecepcion("")
                        .build()
        );

        byte[] pdf = service.exportarPdf(
                JasperReportService.PLANTILLA_TRANSFERENCIA_DETALLE,
                Map.of(
                        "TITULO", "Comprobante de transferencia",
                        "SUBTITULO", "Para firma de responsables",
                        "SISTEMA", "Ultron System",
                        "FECHA_GENERACION", "20/09/2026 11:00:00",
                        "COLUMNA_EXTRA", "",
                        "CANTIDAD", filas.size(),
                        "FILTRO", ""
                ),
                filas
        );

        assertThat(pdf).isNotEmpty();
        assertThat(pdf).startsWith("%PDF".getBytes());
    }

    @Test
    void paginaCuandoHayMasFilasQueLasQueEntranEnUnaHoja() {
        JasperReportService service = new JasperReportService();
        List<ReporteFila> filas = java.util.stream.IntStream.rangeClosed(1, 80)
                .mapToObj(i -> ReporteFila.builder()
                        .numero(i)
                        .codigo("SRV-%03d".formatted(i))
                        .nombre("SERVICIO " + i)
                        .descripcion("Detalle " + i)
                        .categoria("MANTENIMIENTO")
                        .subcategoria("GENERAL")
                        .precio("100.000 Gs")
                        .extra("")
                        .estado("ACTIVO")
                        .build())
                .toList();

        var print = service.rellenar(
                JasperReportService.PLANTILLA_INVENTARIO,
                Map.of(
                        "TITULO", "Catálogo de servicios",
                        "SUBTITULO", "Listado genérico de inventario",
                        "SISTEMA", "Ultron System",
                        "FECHA_GENERACION", "19/09/2026 19:00:00",
                        "COLUMNA_EXTRA", "DETALLE",
                        "CANTIDAD", filas.size(),
                        "FILTRO", ""
                ),
                filas
        );

        assertThat(print.getPages()).hasSizeGreaterThan(1);
    }
}
