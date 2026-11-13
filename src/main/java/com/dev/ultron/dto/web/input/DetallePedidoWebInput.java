package com.dev.ultron.dto.web.input;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetallePedidoWebInput {
    private Long idProducto;
    private Long idPresentacionProducto;
    private BigDecimal cantidad;
}
