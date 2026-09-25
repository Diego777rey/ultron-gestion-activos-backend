package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionProductoOutput {
    private Long id_presentacion_producto;
    private String descripcion;
    private String codigoBarras;
    private BigDecimal cantidad;
    private BigDecimal precio;
}
