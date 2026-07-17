package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaOutput implements Serializable {
    private Long id_detalle_venta;
    private Long idProducto;
    private String productoNombre;
    private Long idPresentacion;
    private String presentacionDescripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
