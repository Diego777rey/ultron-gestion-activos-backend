package com.dev.ultron.dto.personas.input;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Input para registrar o actualizar un cliente.
 * Incluye los datos de persona anidados.
 */
public record ClienteInput(
        PersonaInput persona,
        String ruc,
        String tipoCliente,
        BigDecimal limiteCredito,
        String fechaRegistro,
        String observaciones,
        Boolean estado) implements Serializable {
}
