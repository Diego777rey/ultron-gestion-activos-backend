package com.dev.ultron.service.reportes;

import com.dev.ultron.dto.reportes.TipoReporte;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReporteGenericoServiceTest {

    @Test
    void tituloDelegaEnLaFuenteRegistrada() {
        ReporteGenericoService service = new ReporteGenericoService(
                new JasperReportService(),
                List.of(new FuenteStub())
        );

        assertThat(service.titulo("producto")).isEqualTo("Catálogo de productos");
        assertThat(service.nombreArchivo("producto", null)).isEqualTo("reporte-productos.pdf");
        assertThat(service.nombreArchivo("producto", 12L)).isEqualTo("reporte-productos-12.pdf");
    }

    @Test
    void tituloRechazaTipoSinFuente() {
        ReporteGenericoService service = new ReporteGenericoService(
                new JasperReportService(),
                List.of()
        );

        assertThatThrownBy(() -> service.titulo("producto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No hay una fuente de reporte registrada");
    }

    private static final class FuenteStub implements ReporteFuente {
        @Override
        public TipoReporte tipo() {
            return TipoReporte.PRODUCTO;
        }

        @Override
        public String titulo() {
            return "Catálogo de productos";
        }

        @Override
        public String subtitulo() {
            return "Listado";
        }

        @Override
        public String etiquetaColumnaExtra() {
            return "STOCK";
        }

        @Override
        public String nombreArchivo() {
            return "reporte-productos.pdf";
        }

        @Override
        public List<?> listar(String filtro) {
            return List.of();
        }

        @Override
        public List<?> buscarPorId(Long id) {
            return List.of();
        }
    }
}
