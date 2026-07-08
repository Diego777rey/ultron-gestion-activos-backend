package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ServicioOutput {
    private Long id_servicio;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean estado;
    private CategoriaServicioOutput categoriaServicio;
}
