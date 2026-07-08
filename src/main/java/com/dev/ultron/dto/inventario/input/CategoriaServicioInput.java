package com.dev.ultron.dto.inventario.input;

import lombok.Data;

@Data
public class CategoriaServicioInput {
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private Long idCategoriaPadre;
}
