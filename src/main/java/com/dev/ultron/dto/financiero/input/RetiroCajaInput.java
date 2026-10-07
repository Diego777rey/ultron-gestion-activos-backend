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
public class RetiroCajaInput implements Serializable {
    private Long idSesionCaja;
    /** PYG, BRL o USD (también acepta el nombre: "Dólar", "Real"...). */
    private String moneda;
    private BigDecimal monto;
    private String observacion;
    private Long idUsuarioResponsable;
}
