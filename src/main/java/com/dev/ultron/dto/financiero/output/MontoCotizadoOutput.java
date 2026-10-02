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
public class MontoCotizadoOutput implements Serializable {
    private String moneda;
    private BigDecimal valorCotizacion;
    private BigDecimal monto;
}
