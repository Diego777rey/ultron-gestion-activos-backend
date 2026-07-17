package com.dev.ultron.dto.sectores.output;

import lombok.Data;

@Data
public class ZonaOutput {
    private Long id_zona;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private SectorOutput sector;
}
