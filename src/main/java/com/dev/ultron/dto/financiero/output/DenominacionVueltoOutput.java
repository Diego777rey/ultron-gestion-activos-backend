package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** Cuántas unidades de un billete o moneda se entregan como parte del vuelto. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DenominacionVueltoOutput implements Serializable {
    private BigDecimal valor;
    /** BILLETE o MONEDA. */
    private String tipo;
    private Integer cantidad;
    private BigDecimal subtotal;
}
