package com.dev.ultron.service.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.web.DetallePedidoWeb;
import com.dev.ultron.domain.web.PedidoWeb;
import com.dev.ultron.dto.web.input.DetallePedidoWebInput;
import com.dev.ultron.dto.web.input.PagoTarjetaInput;
import com.dev.ultron.dto.web.input.PedidoWebInput;
import com.dev.ultron.dto.web.output.DetallePedidoWebOutput;
import com.dev.ultron.dto.web.output.PedidoWebOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.web.PedidoWebRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoWebService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final PedidoWebRepository pedidoWebRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public PedidoWebOutput registrarPedido(PedidoWebInput input) {
        validarCliente(input);
        if (input.getDetalles() == null || input.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        String formaPago = normalizar(input.getFormaPago());
        if (formaPago.isEmpty()) {
            formaPago = "TARJETA";
        }

        PedidoWeb pedido = PedidoWeb.builder()
                .codigo(generarCodigo())
                .estado("PENDIENTE")
                .formaPago(formaPago)
                .moneda("PYG")
                .clienteNombre(normalizarNombre(input.getClienteNombre()))
                .clienteEmail(normalizarEmail(input.getClienteEmail()))
                .clienteTelefono(trimToNull(input.getClienteTelefono()))
                .clienteDocumento(trimToNull(input.getClienteDocumento()))
                .direccionEnvio(trimToNull(input.getDireccionEnvio()))
                .ciudad(trimToNull(input.getCiudad()))
                .notas(trimToNull(input.getNotas()))
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedidoWebInput linea : input.getDetalles()) {
            DetallePedidoWeb detalle = armarDetalle(pedido, linea);
            pedido.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        ResultadoPago pago = procesarPago(formaPago, pedido.getTotal(), input.getPagoTarjeta());
        pedido.setPagoTransaccionId(pago.transaccionId());
        pedido.setPagoUltimosCuatro(pago.ultimosCuatro());
        pedido.setPagoMensaje(pago.mensaje());
        pedido.setEstado(pago.aprobado() ? "PAGADO" : "RECHAZADO");

        if (!pago.aprobado()) {
            throw new IllegalArgumentException(pago.mensaje());
        }

        PedidoWeb guardado = pedidoWebRepository.save(pedido);
        return toOutput(guardado);
    }

    @Transactional(readOnly = true)
    public PedidoWebOutput buscarPorCodigo(String codigo) {
        return pedidoWebRepository.findByCodigoConDetalles(codigo)
                .map(this::toOutput)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el pedido " + codigo));
    }

    private DetallePedidoWeb armarDetalle(PedidoWeb pedido, DetallePedidoWebInput linea) {
        if (linea == null || linea.getIdProducto() == null) {
            throw new IllegalArgumentException("Cada ítem del carrito debe indicar el producto");
        }
        if (linea.getCantidad() == null || linea.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepository.findCatalogoWebPorId(linea.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto " + linea.getIdProducto() + " no está disponible en la tienda web"));

        PresentacionProducto presentacion = null;
        BigDecimal precio = producto.getPrecioVenta();
        String presentacionDesc = null;
        if (linea.getIdPresentacionProducto() != null) {
            presentacion = producto.getPresentaciones().stream()
                    .filter(p -> linea.getIdPresentacionProducto().equals(p.getId_presentacion_producto()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La presentación no pertenece al producto " + producto.getNombre()));
            precio = presentacion.getPrecio();
            presentacionDesc = presentacion.getDescripcion();
        }

        BigDecimal cantidad = linea.getCantidad().setScale(3, RoundingMode.HALF_UP);
        BigDecimal subtotal = precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

        return DetallePedidoWeb.builder()
                .pedido(pedido)
                .producto(producto)
                .presentacion(presentacion)
                .nombreProducto(producto.getNombre())
                .presentacionDescripcion(presentacionDesc)
                .cantidad(cantidad)
                .precioUnitario(precio)
                .subtotal(subtotal)
                .build();
    }

    private ResultadoPago procesarPago(String formaPago, BigDecimal total, PagoTarjetaInput tarjeta) {
        if (!"TARJETA".equalsIgnoreCase(formaPago)) {
            String txn = "txn-manual-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            return new ResultadoPago(true, txn, null,
                    "Pedido registrado. Formato de pago: " + formaPago + ". Total Gs. "
                            + total.toPlainString());
        }
        if (tarjeta == null) {
            throw new IllegalArgumentException("Completá los datos de la tarjeta para pagar");
        }
        String numero = tarjeta.getNumero() == null ? "" : tarjeta.getNumero().replaceAll("\\D", "");
        String ultimos = numero.length() >= 4 ? numero.substring(numero.length() - 4) : "";
        String error = validarTarjeta(tarjeta, numero);
        if (error != null) {
            return new ResultadoPago(false, "", ultimos, error);
        }
        if (numero.endsWith("0000")) {
            return new ResultadoPago(false, "", ultimos,
                    "El emisor rechazó el pago. Probá con otra tarjeta.");
        }
        String txn = "txn-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return new ResultadoPago(true, txn, ultimos,
                "Pago de Gs. " + total.toPlainString() + " aprobado.");
    }

    private String validarTarjeta(PagoTarjetaInput tarjeta, String numero) {
        if (tarjeta.getTitular() == null || tarjeta.getTitular().trim().length() < 3) {
            return "Ingresá el nombre del titular como figura en la tarjeta.";
        }
        if (numero.length() < 13 || numero.length() > 19) {
            return "Número de tarjeta inválido.";
        }
        String vencimiento = tarjeta.getVencimiento() == null ? "" : tarjeta.getVencimiento().trim();
        if (!vencimiento.matches("^\\d{2}/\\d{2}$")) {
            return "Vencimiento inválido. Usá formato MM/AA.";
        }
        String cvc = tarjeta.getCvc() == null ? "" : tarjeta.getCvc().trim();
        if (!cvc.matches("^\\d{3,4}$")) {
            return "CVC inválido.";
        }
        return null;
    }

    private void validarCliente(PedidoWebInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Faltan los datos del pedido");
        }
        if (input.getClienteNombre() == null || input.getClienteNombre().trim().length() < 3) {
            throw new IllegalArgumentException("Ingresá el nombre completo del comprador");
        }
        String email = normalizarEmail(input.getClienteEmail());
        if (email.isEmpty() || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Ingresá un correo electrónico válido");
        }
    }

    private String generarCodigo() {
        String codigo;
        do {
            codigo = "WEB-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase(Locale.ROOT)
                    + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        } while (pedidoWebRepository.existsByCodigo(codigo));
        return codigo;
    }

    private PedidoWebOutput toOutput(PedidoWeb pedido) {
        PedidoWebOutput output = new PedidoWebOutput();
        output.setId_pedido_web(pedido.getId_pedido_web());
        output.setCodigo(pedido.getCodigo());
        output.setEstado(pedido.getEstado());
        output.setFormaPago(pedido.getFormaPago());
        output.setMoneda(pedido.getMoneda());
        output.setTotal(pedido.getTotal());
        output.setClienteNombre(pedido.getClienteNombre());
        output.setClienteEmail(pedido.getClienteEmail());
        output.setClienteTelefono(pedido.getClienteTelefono());
        output.setClienteDocumento(pedido.getClienteDocumento());
        output.setDireccionEnvio(pedido.getDireccionEnvio());
        output.setCiudad(pedido.getCiudad());
        output.setNotas(pedido.getNotas());
        output.setPagoTransaccionId(pedido.getPagoTransaccionId());
        output.setPagoUltimosCuatro(pedido.getPagoUltimosCuatro());
        output.setPagoMensaje(pedido.getPagoMensaje());
        output.setCreadoEn(pedido.getCreadoEn() == null ? null : pedido.getCreadoEn().toString());
        List<DetallePedidoWebOutput> detalles = new ArrayList<>();
        if (pedido.getDetalles() != null) {
            for (DetallePedidoWeb detalle : pedido.getDetalles()) {
                DetallePedidoWebOutput linea = new DetallePedidoWebOutput();
                linea.setId_detalle_pedido_web(detalle.getId_detalle_pedido_web());
                linea.setIdProducto(detalle.getProducto() != null ? detalle.getProducto().getId_producto() : null);
                linea.setIdPresentacionProducto(
                        detalle.getPresentacion() != null ? detalle.getPresentacion().getId_presentacion_producto() : null);
                linea.setNombreProducto(detalle.getNombreProducto());
                linea.setPresentacion(detalle.getPresentacionDescripcion());
                linea.setCantidad(detalle.getCantidad());
                linea.setPrecioUnitario(detalle.getPrecioUnitario());
                linea.setSubtotal(detalle.getSubtotal());
                detalles.add(linea);
            }
        }
        output.setDetalles(detalles);
        return output;
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizarNombre(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
    }

    private static String normalizarEmail(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    private record ResultadoPago(boolean aprobado, String transaccionId, String ultimosCuatro, String mensaje) {
    }
}
