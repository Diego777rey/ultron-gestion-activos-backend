package com.dev.ultron.dto.financiero.input;

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
public class MaletinInput implements Serializable {
    private String nombre;
    private BigDecimal balancePyg;
    private BigDecimal balanceUsd;
    private BigDecimal balanceBrl;
    private Long idResponsable;
    private Boolean activo;
}
