package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionProductoInput {
    private Long idProducto;
    private String descripcion;
    private String tipo;
    private BigDecimal cantidad;
    private String codigoBarras;
    private BigDecimal precio;
    private Boolean principal;
    private Boolean estado;
}
