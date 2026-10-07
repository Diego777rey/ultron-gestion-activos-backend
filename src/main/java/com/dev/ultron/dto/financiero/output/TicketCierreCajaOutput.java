package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Datos listos para imprimir el ticket de cierre de caja: el frontend solo los ubica en el papel. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCierreCajaOutput implements Serializable {
    private Long idSesionCaja;
    private String caja;
    private String maletin;
    private String cajero;
    /** dd/MM/yyyy HH:mm */
    private String fechaApertura;
    private String fechaCierre;
    /** Una entrada por moneda (PYG, BRL, USD), aunque no tenga billetes. */
    private List<ConteoMoneda> conteoApertura;
    private List<ConteoMoneda> conteoCierre;
    private Integer cantidadVentas;
    private BigDecimal totalVentasPyg;
    private List<VentasFormaPago> ventasPorFormaPago;
    private List<Retiro> retiros;
    /** Una entrada por moneda (PYG, BRL, USD) con esperado, contado y diferencia. */
    private List<ArqueoMonedaOutput> arqueo;
    /** Cierre contra el que se comparó la apertura; null si el maletín no tenía cierres. */
    private Long idSesionAnterior;
    private String fechaCierreAnterior;
    private List<DiferenciaMoneda> diferencias;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConteoMoneda implements Serializable {
        private String moneda;
        /** Solo denominaciones con cantidad, de menor a mayor. */
        private List<ConteoLinea> lineas;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConteoLinea implements Serializable {
        private BigDecimal valor;
        private Integer cantidad;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentasFormaPago implements Serializable {
        private String formaPago;
        private Integer cantidad;
        /** En guaraníes. */
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Retiro implements Serializable {
        /** dd/MM HH:mm */
        private String fecha;
        private String moneda;
        private BigDecimal monto;
        private String responsable;
        private String observacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiferenciaMoneda implements Serializable {
        private String moneda;
        /** Null si no hay cierre anterior. */
        private BigDecimal cierreAnterior;
        private BigDecimal apertura;
        /** Apertura menos cierre anterior. */
        private BigDecimal diferencia;
    }
}
