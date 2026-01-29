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
public class MovimientoCajaOutput implements Serializable {
    private Long id_movimiento;
    private Long id_caja;
    private String tipo;
    private BigDecimal monto;
    private String concepto;
    private LocalDateTime fecha;
    private Long id_persona;
    private Long id_autorizador;
    private String referencia;
}
