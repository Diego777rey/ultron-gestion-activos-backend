package com.dev.ultron.dto.financiero.output;

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
public class ConteoDenominacionOutput implements Serializable {
    private Long id_conteo;
    private String tipo;
    private String moneda;
    private BigDecimal valorDenominacion;
    private Integer cantidad;
}
