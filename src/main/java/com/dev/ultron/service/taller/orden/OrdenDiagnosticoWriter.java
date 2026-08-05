package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenDiagnostico;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.taller.input.OrdenDiagnosticoInput;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Escribe la pieza de diagnóstico / presupuesto de la orden.
 */
@Component
public class OrdenDiagnosticoWriter {

    public void aplicar(OrdenDiagnostico diagnostico, OrdenDiagnosticoInput input) {
        if (input == null || diagnostico == null) {
            return;
        }
        if (input.fecha_inicio_estimada() != null && !input.fecha_inicio_estimada().isBlank()) {
            diagnostico.setFechaInicioEstimada(parseDateTime(input.fecha_inicio_estimada()));
        }
        if (input.fecha_fin_estimada() != null && !input.fecha_fin_estimada().isBlank()) {
            diagnostico.setFechaFinEstimada(parseDateTime(input.fecha_fin_estimada()));
        }
        if (input.presupuesto_aprobado() != null) {
            diagnostico.setPresupuestoAprobado(input.presupuesto_aprobado());
        }
        if (input.observaciones() != null) {
            diagnostico.setObservaciones(
                    input.observaciones().isBlank() ? null : input.observaciones().toUpperCase());
        }
    }

    public void recalcularTotalPresupuesto(OrdenTrabajo orden) {
        BigDecimal total = orden.getDetalles().stream()
                .map(OrdenTrabajoDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.ensureDiagnostico().setTotalPresupuesto(total);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value.contains("T")) {
            return LocalDateTime.parse(value.length() > 19 ? value.substring(0, 19) : value);
        }
        return LocalDateTime.parse(value + "T00:00:00");
    }
}
