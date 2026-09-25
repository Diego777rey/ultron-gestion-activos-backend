package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionProductoInput {
    private Long id_presentacion_producto;
    private String descripcion;
    private String codigoBarras;
    private BigDecimal cantidad;
    private BigDecimal precio;
}
