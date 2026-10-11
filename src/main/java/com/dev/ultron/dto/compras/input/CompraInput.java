package com.dev.ultron.dto.compras.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CompraInput(
    Long idProveedor,
    Long idVendedor,
    String numeroFactura,
    String timbrado,
    LocalDate fechaFactura,
    String formaPago,
    String moneda,
    BigDecimal cotizacion,
    String estado,
    String observaciones,
    List<ItemCompraInput> items
) {
}
