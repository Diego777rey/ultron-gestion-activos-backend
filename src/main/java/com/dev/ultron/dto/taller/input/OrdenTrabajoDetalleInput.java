package com.dev.ultron.dto.taller.input;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Input para agregar un detalle (producto o servicio) a la orden de trabajo.
 */
public record OrdenTrabajoDetalleInput(
        String tipo,
        Long id_producto,
        Long id_servicio,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precio_unitario
) implements Serializable {
}
