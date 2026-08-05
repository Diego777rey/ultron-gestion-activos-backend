package com.dev.ultron.dto.taller.input;

import java.io.Serializable;

public record OrdenEstadoVehiculoInput(
        Boolean falla_mecanica,
        Boolean falla_electrica,
        Boolean estado_llantas,
        Boolean estado_pintura,
        Boolean estado_rayones,
        Boolean estado_golpes,
        Boolean estado_vidrios,
        String nivel_combustible,
        Integer kilometraje,
        String observaciones_estado
) implements Serializable {
}
