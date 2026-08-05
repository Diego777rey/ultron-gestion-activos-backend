package com.dev.ultron.dto.taller.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEstadoVehiculoOutput implements Serializable {
    private Boolean falla_mecanica;
    private Boolean falla_electrica;
    private Boolean estado_llantas;
    private Boolean estado_pintura;
    private Boolean estado_rayones;
    private Boolean estado_golpes;
    private Boolean estado_vidrios;
    private String nivel_combustible;
    private Integer kilometraje;
    private String observaciones_estado;
}
