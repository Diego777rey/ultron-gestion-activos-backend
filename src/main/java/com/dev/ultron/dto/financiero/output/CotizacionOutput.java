package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionOutput implements Serializable {
    private Long id_cotizacion;
    private String moneda;
    private BigDecimal valor;
    private LocalDateTime fechaActualizacion;
    private Boolean activa;
}
