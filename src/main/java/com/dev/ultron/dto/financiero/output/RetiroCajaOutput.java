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
public class RetiroCajaOutput implements Serializable {
    private Long id_retiro_caja;
    private Long idSesionCaja;
    private String moneda;
    private BigDecimal monto;
    private String observacion;
    private LocalDateTime fecha;
    private Long idUsuarioResponsable;
    private String responsableUsuario;
    /** Nombre y apellido del funcionario; si no tiene, el usuario. */
    private String responsableNombre;
    private String registradoPor;
}
