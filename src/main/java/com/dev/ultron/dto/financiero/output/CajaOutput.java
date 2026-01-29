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
public class CajaOutput implements Serializable {
    private Long id_caja;
    private String nombre;
    private BigDecimal saldo_actual;
    private Long id_empresa;
    private Long id_responsable;
    private Boolean activa;
}
