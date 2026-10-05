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
    private String formaPago;
    private String moneda;
    private BigDecimal montoMonedaOriginal;
    /** Efectivo entregado por el cliente, en la moneda de la venta ({@code moneda}). Solo aplica a EFECTIVO. */
    private BigDecimal montoRecibido;
    /** Moneda en la que el cajero entrega el vuelto. PYG por defecto. */
    private String monedaVuelto;
    private List<DetalleVentaInput> detalles;
}
