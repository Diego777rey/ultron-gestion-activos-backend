package com.dev.ultron.dto.inventario.output;

import lombok.Data;

@Data
public class CategoriaProductoOutput {
    private Long id_categoria_producto;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private CategoriaProductoOutput categoriaPadre;
}
