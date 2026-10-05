package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del cálculo de vuelto de un cobro en efectivo.
 * El cliente puede pagar en cualquier moneda con cotización activa y el cajero
 * puede devolver en cualquier otra; todo se concilia en guaraníes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VueltoOutput implements Serializable {
    private BigDecimal totalPyg;

    // ---- Lo que entrega el cliente ----
    /** Moneda en la que paga el cliente (PYG, USD, BRL...). */
    private String monedaRecibida;
    /** Guaraníes por unidad de la moneda recibida. {@code null} cuando es PYG. */
    private BigDecimal cotizacionRecibida;
    /** Total a cobrar expresado en la moneda recibida. */
    private BigDecimal totalMonedaRecibida;
    /** Lo que entregó el cliente, en la moneda recibida. {@code null} si todavía no se informó. */
    private BigDecimal montoRecibido;
    /** Equivalente en guaraníes de {@code montoRecibido}. */
    private BigDecimal montoRecibidoPyg;
    /** Cuánto falta para cubrir el total, en la moneda recibida. Cero si alcanza. */
    private BigDecimal faltante;
    /** {@code true} cuando el monto recibido cubre el total. */
    private Boolean suficiente;
    /** Importes que el cliente suele entregar, en la moneda recibida (el primero es el exacto). */
    private List<BigDecimal> montosSugeridos;

    // ---- Lo que devuelve el cajero ----
    /** Moneda en la que se entrega el vuelto. */
    private String monedaVuelto;
    /** Guaraníes por unidad de la moneda del vuelto. {@code null} cuando es PYG. */
    private BigDecimal cotizacionVuelto;
    /** Vuelto en guaraníes (diferencia entre lo recibido y el total). */
    private BigDecimal vueltoPyg;
    /** Vuelto expresado en {@code monedaVuelto}. Cero si no alcanza o no se informó. */
    private BigDecimal vuelto;
    /** {@code true} si la moneda del vuelto tiene catálogo de billetes y monedas para desglosar. */
    private Boolean desgloseDisponible;
    /** Billetes y monedas de {@code monedaVuelto} que arman el vuelto, de mayor a menor. */
    private List<DenominacionVueltoOutput> desglose;
    /** Parte del vuelto (en {@code monedaVuelto}) que no se puede representar con denominaciones. */
    private BigDecimal residuo;
}
