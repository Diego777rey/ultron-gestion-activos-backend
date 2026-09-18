package com.dev.ultron.dto.reportes.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila del reporte detallado de orden de trabajo.
 * Los nombres coinciden con los {@code <field>} de {@code reports/orden_trabajo_detalle.jrxml}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteOtDetalleFila {

    private String numeroOrden;
    private String etapa;
    private String fecha;
    private String presupuesto;
    private String encabezado;
    private String tipoLinea;
    private String nombreLinea;
    private String cantidad;
    private String precio;
    private String subtotal;
}
