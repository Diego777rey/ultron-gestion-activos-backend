package com.dev.ultron.dto.inventario.output;

import lombok.Data;

import java.util.List;

@Data
public class CategoriaProductoOutput {
    private Long id_categoria_producto;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private CategoriaProductoOutput categoriaPadre;
    /** Subcategorías directas. El menú de la tienda las completa en cada familia. */
    private List<CategoriaProductoOutput> subcategorias;
}
