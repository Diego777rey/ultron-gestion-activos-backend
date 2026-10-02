package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Output DTO de DetalleFactura para respuestas GraphQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFacturaOutput implements Serializable {
    private Long id_detalle_factura;
    private Long id_producto;
    private Long id_servicio;
    private Long id_presentacion;
    private String descripcion;
    private String codigo;
    private BigDecimal cantidad;
    private BigDecimal precio_unitario;
    private BigDecimal subtotal;
    private String tipo_iva;
    private BigDecimal monto_iva;
    private BigDecimal total_linea;
    private Integer numero_linea;
}
