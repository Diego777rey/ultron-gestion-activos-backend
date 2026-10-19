package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.DetalleVenta;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.financiero.output.LineaOrdenVentaOutput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Desglose de una orden cobrada: cada servicio y cada producto del presupuesto,
 * para que el ticket muestre lo aplicado y no solo el número de la orden.
 */
public final class OrdenVentaLineas {

    private static final String IVA_10 = "10";

    private OrdenVentaLineas() {
    }

    public static List<OrdenTrabajoDetalle> partes(DetalleVenta detalle) {
        if (detalle == null || detalle.getOrdenTrabajo() == null || detalle.getOrdenTrabajo().getDetalles() == null) {
            return List.of();
        }
        return detalle.getOrdenTrabajo().getDetalles().stream().filter(Objects::nonNull).toList();
    }

    /**
     * Líneas del ticket de venta. Null si la orden no tiene ítems o si la suma
     * no coincide con lo cobrado: en ese caso el ticket conserva la línea única.
     */
    public static List<LineaOrdenVentaOutput> lineasTicket(DetalleVenta detalle) {
        List<OrdenTrabajoDetalle> partes = partes(detalle);
        if (partes.isEmpty() || !sumaCoincide(detalle, partes)) {
            return null;
        }
        OrdenTrabajo orden = detalle.getOrdenTrabajo();
        return partes.stream()
                .map(parte -> LineaOrdenVentaOutput.builder()
                        .descripcion(descripcion(orden, parte))
                        .cantidad(cantidad(parte))
                        .precioUnitario(precio(parte))
                        .subtotal(subtotalEntero(parte))
                        .build())
                .toList();
    }

    public static boolean sumaCoincide(DetalleVenta detalle, List<OrdenTrabajoDetalle> partes) {
        BigDecimal cobrado = detalle.getSubtotal() == null
                ? BigDecimal.ZERO
                : detalle.getSubtotal().setScale(0, RoundingMode.HALF_UP);
        return subtotal(partes).compareTo(cobrado) == 0;
    }

    public static BigDecimal subtotal(List<OrdenTrabajoDetalle> partes) {
        return partes.stream().map(OrdenVentaLineas::subtotalEntero).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal subtotalEntero(OrdenTrabajoDetalle parte) {
        BigDecimal subtotal = parte.getSubtotal();
        if (subtotal == null) {
            subtotal = cantidad(parte).multiply(precio(parte));
        }
        return subtotal.setScale(0, RoundingMode.HALF_UP);
    }

    public static String descripcion(OrdenTrabajo orden, OrdenTrabajoDetalle parte) {
        String nombre = nombre(parte);
        String numero = orden != null && orden.getNumeroOrden() != null ? orden.getNumeroOrden().trim() : "";
        if (numero.isEmpty()) {
            return nombre;
        }
        return numero + " - " + nombre;
    }

    public static String tipoIva(OrdenTrabajoDetalle parte) {
        Producto producto = parte.getProducto();
        if (producto == null || producto.getTipoIva() == null || producto.getTipoIva().isBlank()) {
            return IVA_10;
        }
        String valor = producto.getTipoIva().trim().toUpperCase();
        if ("5".equals(valor) || "10".equals(valor) || "EXENTA".equals(valor)) {
            return valor;
        }
        return IVA_10;
    }

    public static Long idProducto(OrdenTrabajoDetalle parte) {
        return parte.getProducto() != null ? parte.getProducto().getId_producto() : null;
    }

    public static Long idServicio(OrdenTrabajoDetalle parte) {
        return parte.getServicio() != null ? parte.getServicio().getId_servicio() : null;
    }

    public static String codigo(OrdenTrabajoDetalle parte) {
        Producto producto = parte.getProducto();
        if (producto != null && producto.getCodigo() != null && !producto.getCodigo().isBlank()) {
            return producto.getCodigo();
        }
        Servicio servicio = parte.getServicio();
        if (servicio != null && servicio.getCodigo() != null && !servicio.getCodigo().isBlank()) {
            return servicio.getCodigo();
        }
        return null;
    }

    public static BigDecimal cantidad(OrdenTrabajoDetalle parte) {
        if (parte.getCantidad() == null || parte.getCantidad().signum() <= 0) {
            return BigDecimal.ONE;
        }
        return parte.getCantidad();
    }

    private static BigDecimal precio(OrdenTrabajoDetalle parte) {
        return parte.getPrecioUnitario() != null ? parte.getPrecioUnitario() : BigDecimal.ZERO;
    }

    private static String nombre(OrdenTrabajoDetalle parte) {
        if (parte.getDescripcion() != null && !parte.getDescripcion().isBlank()) {
            return parte.getDescripcion().trim();
        }
        if (parte.getServicio() != null && parte.getServicio().getNombre() != null
                && !parte.getServicio().getNombre().isBlank()) {
            return parte.getServicio().getNombre().trim();
        }
        if (parte.getProducto() != null && parte.getProducto().getNombre() != null
                && !parte.getProducto().getNombre().isBlank()) {
            return parte.getProducto().getNombre().trim();
        }
        return "ITEM";
    }
}
