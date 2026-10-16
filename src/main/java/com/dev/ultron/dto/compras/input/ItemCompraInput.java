package com.dev.ultron.dto.compras.input;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemCompraInput(
    Long idProducto,
    BigDecimal cantidad,
    BigDecimal precioCosto,
    String tipoIva,
    LocalDate fechaVencimiento,
    String lote,
    String observaciones
) {
}
