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
public class CajaInput implements Serializable {
    private String nombre;
    private BigDecimal saldoActual;
    private Long idEmpresa;
    private Long idResponsable;
    private Long idSector;
    private Boolean activa;
}
