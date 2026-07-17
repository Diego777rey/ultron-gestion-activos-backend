package com.dev.ultron.dto.sectores.input;

import lombok.Data;

@Data
public class ZonaInput {
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private Long idSector;
}
