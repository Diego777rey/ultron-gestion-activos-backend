package com.dev.ultron.dto.reportes.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila del reporte de facturas legales. Los nombres coinciden con los
 * {@code <field>} de {@code reports/facturas_legales.jrxml}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFacturaFila {

    private String numeroFactura;
    private String fecha;
    private String cliente;
    private String ruc;
    private String timbrado;
    private String iva10;
    private String iva5;
    private String exenta;
    private String total;
    private String formaPago;
    private String estado;
}
