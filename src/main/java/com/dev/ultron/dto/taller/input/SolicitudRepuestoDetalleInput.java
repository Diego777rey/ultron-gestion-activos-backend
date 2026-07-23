package com.dev.ultron.dto.taller.input;

import java.io.Serializable;
import java.math.BigDecimal;

public record SolicitudRepuestoDetalleInput(
        Long id_producto,
        BigDecimal cantidad
) implements Serializable {
}
