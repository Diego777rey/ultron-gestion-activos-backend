package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaOutput implements Serializable {
    private Long id_detalle_venta;
    private Long idProducto;
    private Long idPresentacion;
    private String presentacionDescripcion;
    private Long idOrdenTrabajo;
    private Long idServicio;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    /** Servicios y productos de la orden, cuando el cobro se puede desglosar. */
    private List<LineaOrdenVentaOutput> lineasOrden;
}
