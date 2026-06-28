package com.dev.ultron.dto.personas.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Output DTO de Cliente para respuestas GraphQL.
 * Incluye los datos de persona anidados como PersonaOutput.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteOutput implements Serializable {
    private Long id_cliente;
    private PersonaOutput persona;
    private String ruc;
    private String tipoCliente;
    private BigDecimal limiteCredito;
    private String fechaRegistro;
    private String observaciones;
    private Boolean estado;
}
