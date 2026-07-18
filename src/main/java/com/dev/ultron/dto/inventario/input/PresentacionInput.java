package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionInput {
    private String nombre;
    private String descripcion;
    private BigDecimal cantidad;
    private Boolean estado;
}
