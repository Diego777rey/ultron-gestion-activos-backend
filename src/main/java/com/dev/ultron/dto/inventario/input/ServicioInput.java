package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ServicioInput {
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean estado;
    private Long idCategoriaServicio;
}
