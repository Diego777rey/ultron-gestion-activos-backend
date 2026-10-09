package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** Arqueo de una moneda: esperado = apertura + cobrosEfectivo - vueltos - retiros. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArqueoMonedaOutput implements Serializable {
    /** PYG, BRL o USD. */
    private String moneda;
    private BigDecimal apertura;
    private BigDecimal cobrosEfectivo;
    private BigDecimal vueltos;
    private BigDecimal retiros;
    private BigDecimal esperado;
    /** Conteo de cierre; null mientras la sesión está abierta. */
    private BigDecimal contado;
    /** Contado menos esperado: negativo es faltante. Null mientras la sesión está abierta. */
    private BigDecimal diferencia;
}
