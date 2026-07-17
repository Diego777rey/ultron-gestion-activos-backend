package com.dev.ultron.dto.financiero.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaInput implements Serializable {
    private Long idSesionCaja;
    private Long idCliente;
    private BigDecimal descuento;
    private List<DetalleVentaInput> detalles;
}
