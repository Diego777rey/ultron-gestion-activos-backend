package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenRecepcion;
import com.dev.ultron.dto.taller.input.OrdenRecepcionInput;

import org.springframework.stereotype.Component;

/**
 * Escribe la pieza de recepción de la orden.
 */
@Component
public class OrdenRecepcionWriter {

    public void aplicar(OrdenRecepcion recepcion, OrdenRecepcionInput input) {
        if (input == null || recepcion == null) {
            return;
        }
        if (input.descripcion_falla() != null) {
            recepcion.setDescripcionFalla(
                    input.descripcion_falla().isBlank() ? null : input.descripcion_falla().toUpperCase());
        }
    }
}
