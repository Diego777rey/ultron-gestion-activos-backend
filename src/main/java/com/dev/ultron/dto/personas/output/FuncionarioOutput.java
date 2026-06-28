package com.dev.ultron.dto.personas.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Output DTO de Funcionario para respuestas GraphQL.
 * Incluye los datos de persona anidados como PersonaOutput.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioOutput implements Serializable {
    private Long id_funcionario;
    private PersonaOutput persona;
    private BigDecimal sueldo;
    private String sector;
    private String fechaIngreso;
    private Boolean facePrueba;
    private Boolean estado;
}
