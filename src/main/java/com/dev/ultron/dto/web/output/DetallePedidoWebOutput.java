package com.dev.ultron.dto.web.output;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetallePedidoWebOutput {
    private Long id_detalle_pedido_web;
    private Long idProducto;
    private Long idPresentacionProducto;
    private String nombreProducto;
    private String presentacion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
