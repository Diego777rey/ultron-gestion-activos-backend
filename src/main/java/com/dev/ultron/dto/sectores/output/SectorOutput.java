package com.dev.ultron.dto.sectores.output;

import lombok.Data;

@Data
public class SectorOutput {
    private Long id_sector;
    private String nombre;
    private String descripcion;
    private Boolean estado;
}
