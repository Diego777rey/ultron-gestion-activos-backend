package com.dev.ultron.dto.taller.input;

import java.io.Serializable;
import java.util.List;

/**
 * Input para crear/actualizar una orden de trabajo (core + piezas anidadas).
 * Si viene {@code tipo_recepcion}, vehículo y equipos se reemplazan tal cual llegan (null los quita).
 * {@code id_equipo} se acepta solo si no viene {@code ids_equipos}, para clientes anteriores.
 */
public record OrdenTrabajoInput(
        Long id_sector,
        Long id_responsable,
        Long id_cliente,
        String tipo_recepcion,
        Long id_vehiculo,
        Long id_equipo,
        List<Long> ids_equipos,
        Long id_mecanico,
        List<Long> ids_mecanicos,
        Long id_caja,
        java.math.BigDecimal monto_pago,
        String observaciones_finalizacion,
        OrdenRecepcionInput recepcion,
        OrdenEstadoVehiculoInput estado_vehiculo,
        OrdenDiagnosticoInput diagnostico
) implements Serializable {
}
