package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PresentacionOutput {
    private Long id_presentacion;
    private String nombre;
    private String descripcion;
    private BigDecimal cantidad;
    private Boolean estado;
}
