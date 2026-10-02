package com.dev.ultron.dto.financiero.input;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Input para una línea de detalle de factura.
 */
public record DetalleFacturaInput(
        Long idProducto,
        Long idServicio,
        Long idPresentacion,
        String descripcion,
        String codigo,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String tipoIva
) implements Serializable {
}
