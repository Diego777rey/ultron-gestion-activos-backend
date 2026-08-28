package com.dev.ultron.dto.taller.input;

import java.io.Serializable;

public record OrdenDiagnosticoInput(
        String fecha_inicio_estimada,
        String fecha_fin_estimada,
        Integer duracion_estimada_dias,
        Boolean presupuesto_aprobado,
        String observaciones
) implements Serializable {
}
