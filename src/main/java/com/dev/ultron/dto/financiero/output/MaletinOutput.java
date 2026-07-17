package com.dev.ultron.dto.financiero.output;

import com.dev.ultron.dto.personas.output.PersonaOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaletinOutput implements Serializable {
    private Long id_maletin;
    private String nombre;
    private String estado;
    private BigDecimal balancePyg;
    private BigDecimal balanceUsd;
    private BigDecimal balanceBrl;
    private PersonaOutput responsable;
    private Boolean activo;
}
