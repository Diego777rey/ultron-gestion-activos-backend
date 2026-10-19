package com.dev.ultron.dto.reportes.mapper;

import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.dto.reportes.output.ReporteFacturaFila;
import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.DateUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Arma una fila de facturación con los mismos datos que el listado:
 * número, fecha, cliente, RUC, timbrado, IVA y total.
 */
public final class ReporteFacturaMapper {

    private static final Locale LOCALE_PY = Locale.of("es", "PY");

    private static final ThreadLocal<NumberFormat> FORMATO = ThreadLocal.withInitial(() -> {
        NumberFormat formato = NumberFormat.getInstance(LOCALE_PY);
        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);
        return formato;
    });

    private ReporteFacturaMapper() {
    }

    public static ReporteFacturaFila de(Factura factura) {
        return ReporteFacturaFila.builder()
                .numeroFactura(nvl(factura.getNumero_factura()))
                .fecha(fecha(factura.getFecha_emision()))
                .cliente(nvl(factura.getCliente_nombre()))
                .ruc(primero(factura.getCliente_ruc(), factura.getCliente_documento()))
                .timbrado(nvl(factura.getTimbrado()))
                .iva10(monto(factura.getTotal_iva_10()))
                .iva5(monto(factura.getTotal_iva_5()))
                .exenta(monto(factura.getTotal_exenta()))
                .total(monto(factura.getTotal()) + " " + AppConstants.MONEDA_DEFAULT)
                .formaPago(formaPago(factura.getForma_pago()))
                .estado(nvl(factura.getEstado()))
                .build();
    }

    private static String fecha(LocalDateTime valor) {
        if (valor == null) {
            return "";
        }
        return DateUtil.format(valor.toLocalDate());
    }

    private static String monto(BigDecimal valor) {
        return FORMATO.get().format(valor != null ? valor : BigDecimal.ZERO);
    }

    private static String formaPago(String formaPago) {
        if (formaPago == null || formaPago.isBlank()) {
            return "";
        }
        return switch (formaPago.trim().toUpperCase(Locale.ROOT)) {
            case "EFECTIVO" -> "Efectivo";
            case "TARJETA" -> "Tarjeta";
            case "TRANSFERENCIA" -> "Transferencia";
            case "CHEQUE" -> "Cheque";
            case "CREDITO" -> "Crédito";
            default -> formaPago.trim();
        };
    }

    private static String primero(String preferido, String alternativa) {
        if (preferido != null && !preferido.isBlank()) {
            return preferido.trim();
        }
        return nvl(alternativa);
    }

    private static String nvl(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
