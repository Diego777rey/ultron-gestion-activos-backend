package com.dev.ultron.dto.financiero.output;

import com.dev.ultron.dto.personas.output.PersonaOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionCajaOutput implements Serializable {
    private Long id_sesion_caja;
    private CajaOutput caja;
    private MaletinOutput maletin;
    private PersonaOutput persona;
    private String estado;
    private BigDecimal montoInicialPyg;
    private BigDecimal montoInicialUsd;
    private BigDecimal montoInicialBrl;
    private BigDecimal montoFinalPyg;
    private BigDecimal montoFinalUsd;
    private BigDecimal montoFinalBrl;
    private BigDecimal diferenciaPyg;
    private BigDecimal diferenciaUsd;
    private BigDecimal diferenciaBrl;
    private BigDecimal totalVentasPyg;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private List<ConteoDenominacionOutput> conteos;
}
