package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;

import org.springframework.stereotype.Component;

/**
 * Orquesta la aplicación del input anidado sobre el core y las piezas de la orden.
 */
@Component
public class OrdenTrabajoInputApplier {

    private final OrdenTrabajoActoresWriter actoresWriter;
    private final OrdenRecepcionWriter recepcionWriter;
    private final OrdenEstadoVehiculoWriter estadoVehiculoWriter;
    private final OrdenDiagnosticoWriter diagnosticoWriter;

    public OrdenTrabajoInputApplier(
            OrdenTrabajoActoresWriter actoresWriter,
            OrdenRecepcionWriter recepcionWriter,
            OrdenEstadoVehiculoWriter estadoVehiculoWriter,
            OrdenDiagnosticoWriter diagnosticoWriter) {
        this.actoresWriter = actoresWriter;
        this.recepcionWriter = recepcionWriter;
        this.estadoVehiculoWriter = estadoVehiculoWriter;
        this.diagnosticoWriter = diagnosticoWriter;
    }

    public void aplicar(OrdenTrabajo orden, OrdenTrabajoInput input, boolean creando) {
        actoresWriter.aplicar(orden, input, creando);

        if (input.recepcion() != null) {
            recepcionWriter.aplicar(orden.ensureRecepcion(), input.recepcion());
        }
        if (input.estado_vehiculo() != null) {
            estadoVehiculoWriter.aplicar(orden.ensureEstadoVehiculo(), input.estado_vehiculo());
        }
        if (input.diagnostico() != null) {
            diagnosticoWriter.aplicar(orden.ensureDiagnostico(), input.diagnostico());
        }
    }
}
