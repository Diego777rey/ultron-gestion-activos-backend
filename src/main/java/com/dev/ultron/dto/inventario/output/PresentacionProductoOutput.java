package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionProductoOutput {
    private Long id_presentacion_producto;
    private String descripcion;
    private String tipo;
    private BigDecimal cantidad;
    private String codigoBarras;
    private BigDecimal precio;
    private Boolean principal;
    private Boolean estado;
}
