package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** Servicio o producto de una orden de trabajo, para imprimirlo en el ticket de venta. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineaOrdenVentaOutput implements Serializable {
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
