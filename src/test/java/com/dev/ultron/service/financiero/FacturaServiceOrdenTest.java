package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.DetalleVenta;
import com.dev.ultron.domain.financiero.Venta;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.financiero.input.DetalleFacturaInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FacturaServiceOrdenTest {

    private final FacturaService service = new FacturaService(
            null, null, null, null, null, null, null, null, null, null, null, null);

    @Test
    void facturaLaOrdenServicioPorServicioIgualQueUnProducto() {
        List<DetalleFacturaInput> lineas = service.detallesDesdeVenta(venta(ordenConServicioYProducto(), null));

        assertEquals(2, lineas.size());
        assertEquals("OT-0016 - CAMBIO DE ACEITE", lineas.get(0).descripcion());
        assertEquals(0, new BigDecimal("150000").compareTo(lineas.get(0).precioUnitario()));
        assertEquals(0, BigDecimal.ONE.compareTo(lineas.get(0).cantidad()));
        assertEquals(8L, lineas.get(0).idServicio());
        assertEquals("10", lineas.get(0).tipoIva());
        assertNull(lineas.get(0).idProducto());

        assertEquals("OT-0016 - FILTRO", lineas.get(1).descripcion());
        assertEquals(0, new BigDecimal("2").compareTo(lineas.get(1).cantidad()));
        assertEquals(0, new BigDecimal("24000").compareTo(lineas.get(1).precioUnitario()));
        assertEquals(4L, lineas.get(1).idProducto());
        assertEquals("5", lineas.get(1).tipoIva());
    }

    @Test
    void unProductoSueltoSigueSiendoUnaLinea() {
        Producto producto = Producto.builder()
                .id_producto(3L)
                .nombre("BATERIA 12V")
                .codigo("BAT")
                .tipoIva("10")
                .build();
        DetalleVenta detalle = DetalleVenta.builder()
                .producto(producto)
                .cantidad(new BigDecimal("2"))
                .precioUnitario(new BigDecimal("560000"))
                .subtotal(new BigDecimal("1120000"))
                .build();

        List<DetalleFacturaInput> lineas = service.detallesDesdeVenta(venta(detalle, null));

        assertEquals(1, lineas.size());
        assertEquals("BATERIA 12V", lineas.get(0).descripcion());
        assertEquals(0, new BigDecimal("2").compareTo(lineas.get(0).cantidad()));
        assertEquals(0, new BigDecimal("560000").compareTo(lineas.get(0).precioUnitario()));
    }

    @Test
    void elDescuentoSeReparteEntreLosServiciosSinCambiarElTotal() {
        List<DetalleFacturaInput> lineas = service.detallesDesdeVenta(
                venta(ordenConServicioYProducto(), new BigDecimal("10000")));

        BigDecimal total = lineas.stream()
                .map(linea -> linea.precioUnitario().multiply(linea.cantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("188000").compareTo(total));
        assertEquals("OT-0016 - CAMBIO DE ACEITE", lineas.get(0).descripcion());
        assertEquals("OT-0016 - FILTRO", lineas.get(1).descripcion());
    }

    private static Venta venta(DetalleVenta detalle, BigDecimal descuento) {
        return Venta.builder()
                .descuento(descuento)
                .detalles(new ArrayList<>(List.of(detalle)))
                .build();
    }

    private static DetalleVenta ordenConServicioYProducto() {
        Servicio servicio = Servicio.builder().id_servicio(8L).nombre("CAMBIO DE ACEITE").codigo("SRV").build();
        Producto producto = Producto.builder().id_producto(4L).nombre("FILTRO").codigo("FIL").tipoIva("5").build();
        OrdenTrabajoDetalle cambio = OrdenTrabajoDetalle.builder()
                .tipo("SERVICIO")
                .servicio(servicio)
                .descripcion("CAMBIO DE ACEITE")
                .cantidad(BigDecimal.ONE)
                .precioUnitario(new BigDecimal("150000"))
                .subtotal(new BigDecimal("150000"))
                .build();
        OrdenTrabajoDetalle filtro = OrdenTrabajoDetalle.builder()
                .tipo("PRODUCTO")
                .producto(producto)
                .descripcion("FILTRO")
                .cantidad(new BigDecimal("2"))
                .precioUnitario(new BigDecimal("24000"))
                .subtotal(new BigDecimal("48000"))
                .build();
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .numeroOrden("OT-0016")
                .detalles(new ArrayList<>(List.of(cambio, filtro)))
                .build();
        return DetalleVenta.builder()
                .ordenTrabajo(orden)
                .descripcion("OT-0016 - 6DSADAD")
                .cantidad(BigDecimal.ONE)
                .precioUnitario(new BigDecimal("198000"))
                .subtotal(new BigDecimal("198000"))
                .build();
    }
}
