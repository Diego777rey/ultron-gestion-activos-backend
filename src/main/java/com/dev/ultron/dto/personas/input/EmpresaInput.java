package com.dev.ultron.dto.personas.input;

import java.io.Serializable;

/**
 * Input para registrar o actualizar una empresa.
 */
public record EmpresaInput(
        String razon_social,
        String ruc,
        String direccion,
        String fechaCreacion,
        String telefono,
        String email,
        String actividadEconomica,
        String logo,
        Boolean activa
) implements Serializable {
}
