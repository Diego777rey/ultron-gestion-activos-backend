package com.dev.ultron.dto.personas.input;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Input para registrar o actualizar un funcionario.
 * Incluye los datos de persona anidados.
 */
public record FuncionarioInput(
        PersonaInput persona,
        BigDecimal sueldo,
        String sector,
        String fechaIngreso,
        Boolean facePrueba,
        Boolean estado) implements Serializable {
}
