package com.dev.ultron.dto.inventario.output;

import lombok.Data;

@Data
public class CategoriaServicioOutput {
    private Long id_categoria_servicio;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private CategoriaServicioOutput categoriaPadre;
}
