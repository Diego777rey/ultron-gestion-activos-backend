package com.dev.ultron.dto.inventario.input;

import lombok.Data;

@Data
public class CategoriaProductoInput {
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private Long idCategoriaPadre;
}
