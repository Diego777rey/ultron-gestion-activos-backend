package com.dev.ultron.dto.taller.input;

import java.io.Serializable;
import java.util.List;

public record SolicitudRepuestoInput(
        Long id_sector_origen,
        String observacion,
        List<SolicitudRepuestoDetalleInput> detalles
) implements Serializable {
}
