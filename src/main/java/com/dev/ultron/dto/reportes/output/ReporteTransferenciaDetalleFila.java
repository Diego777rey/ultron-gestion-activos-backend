package com.dev.ultron.dto.reportes.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila del reporte detallado de transferencia.
 * Los nombres coinciden con los {@code <field>} de {@code reports/transferencia_detalle.jrxml}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTransferenciaDetalleFila {

    private String numero;
    private String estado;
    private String fecha;
    private String encabezado;
    private String codigo;
    private String nombreLinea;
    private String cantidad;
    private String estadoLinea;
    private String firmaEntrega;
    private String firmaRecepcion;
}
