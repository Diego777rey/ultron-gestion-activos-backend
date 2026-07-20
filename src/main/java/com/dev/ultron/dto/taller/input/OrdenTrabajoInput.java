package com.dev.ultron.dto.taller.input;

import java.io.Serializable;

/**
 * Input para crear/actualizar una orden de trabajo.
 */
public record OrdenTrabajoInput(
        Long id_sector,
        Long id_responsable,
        Long id_cliente,
        Long id_vehiculo,
        Long id_mecanico,
        String descripcion_falla,
        String fecha_inicio_estimada,
        String fecha_fin_estimada,
        Boolean presupuesto_aprobado,
        String observaciones,
        Long id_caja
) implements Serializable {
}
