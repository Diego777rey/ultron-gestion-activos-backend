package com.dev.ultron.dto.taller.input;

import java.io.Serializable;
import java.util.List;

/**
 * Input para crear/actualizar una orden de trabajo (core + piezas anidadas).
 */
public record OrdenTrabajoInput(
        Long id_sector,
        Long id_responsable,
        Long id_cliente,
        Long id_vehiculo,
        Long id_mecanico,
        List<Long> ids_mecanicos,
        Long id_caja,
        OrdenRecepcionInput recepcion,
        OrdenEstadoVehiculoInput estado_vehiculo,
        OrdenDiagnosticoInput diagnostico
) implements Serializable {
}
