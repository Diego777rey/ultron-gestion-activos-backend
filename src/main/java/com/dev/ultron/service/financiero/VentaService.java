package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.DetalleVenta;
import com.dev.ultron.domain.financiero.Ingreso;
import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.financiero.Venta;
import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.financiero.input.DetalleVentaInput;
import com.dev.ultron.dto.financiero.input.VentaInput;
import com.dev.ultron.dto.financiero.mapper.VentaMapper;
import com.dev.ultron.dto.financiero.output.VentaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.IngresoRepository;
import com.dev.ultron.repository.financiero.MaletinRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.inventario.PresentacionProductoRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.personas.ClienteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VentaService extends GenericCrudService<Venta, Long> {

    private final VentaRepository repository;
    private final VentaMapper mapper;
    private final SesionCajaRepository sesionCajaRepository;
    private final ProductoRepository productoRepository;
    private final PresentacionProductoRepository presentacionProductoRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final IngresoRepository ingresoRepository;
    private final MaletinRepository maletinRepository;
    private final CajaRepository cajaRepository;

    public VentaService(
            VentaRepository repository,
            VentaMapper mapper,
            SesionCajaRepository sesionCajaRepository,
            ProductoRepository productoRepository,
            PresentacionProductoRepository presentacionProductoRepository,
            ClienteRepository clienteRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            IngresoRepository ingresoRepository,
            MaletinRepository maletinRepository,
            CajaRepository cajaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.sesionCajaRepository = sesionCajaRepository;
        this.productoRepository = productoRepository;
        this.presentacionProductoRepository = presentacionProductoRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.ingresoRepository = ingresoRepository;
        this.maletinRepository = maletinRepository;
        this.cajaRepository = cajaRepository;
    }

    @Override
    protected JpaRepository<Venta, Long> getRepository() {
        return repository;
    }

    @Transactional
    public VentaOutput registrarVenta(VentaInput input) {
        if (input.getIdSesionCaja() == null) {
            throw new IllegalArgumentException("Debe indicar la sesión de caja");
        }
        if (input.getDetalles() == null || input.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }

        SesionCaja sesion = sesionCajaRepository.findById(input.getIdSesionCaja())
                .orElseThrow(() -> new EntityNotFoundException("Sesión de caja no encontrada"));
        if (!"ABIERTA".equalsIgnoreCase(sesion.getEstado())) {
            throw new IllegalArgumentException("No se puede vender: la sesión de caja no está abierta");
        }

        Cliente cliente = null;
        if (input.getIdCliente() != null) {
            cliente = clienteRepository.findById(input.getIdCliente())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + input.getIdCliente()));
        }

        BigDecimal descuento = input.getDescuento() != null ? input.getDescuento() : BigDecimal.ZERO;
        if (descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }

        long seq = repository.countBySesion(sesion.getId_sesion_caja()) + 1;
        String numero = "VEN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + sesion.getId_sesion_caja() + "-" + String.format("%04d", seq);

        Venta venta = Venta.builder()
                .numero(numero)
                .fecha(LocalDateTime.now())
                .sesionCaja(sesion)
                .cliente(cliente)
                .descuento(descuento)
                .estado("PAGADA")
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVentaInput detInput : input.getDetalles()) {
            if (detInput.getIdProducto() == null || detInput.getCantidad() == null
                    || detInput.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto y cantidad válida");
            }

            Producto producto = productoRepository.findById(detInput.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + detInput.getIdProducto()));

            PresentacionProducto presentacion = null;
            BigDecimal factor = BigDecimal.ONE;
            BigDecimal precio = detInput.getPrecioUnitario() != null
                    ? detInput.getPrecioUnitario()
                    : producto.getPrecioVenta();

            if (detInput.getIdPresentacion() != null) {
                presentacion = presentacionProductoRepository.findById(detInput.getIdPresentacion())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Presentación no encontrada con id: " + detInput.getIdPresentacion()));
                if (!presentacion.getProducto().getId_producto().equals(producto.getId_producto())) {
                    throw new IllegalArgumentException("La presentación no pertenece al producto indicado");
                }
                factor = presentacion.getCantidad() != null ? presentacion.getCantidad() : BigDecimal.ONE;
                if (detInput.getPrecioUnitario() == null) {
                    precio = presentacion.getPrecio();
                }
            }

            BigDecimal stockADescontar = detInput.getCantidad().multiply(factor);
            BigDecimal stockActual = producto.getStock() != null ? producto.getStock() : BigDecimal.ZERO;
            if (stockActual.compareTo(stockADescontar) < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para " + producto.getNombre()
                                + ". Disponible: " + stockActual + ", requerido: " + stockADescontar);
            }

            BigDecimal lineaSubtotal = precio.multiply(detInput.getCantidad());
            subtotal = subtotal.add(lineaSubtotal);

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .presentacion(presentacion)
                    .cantidad(detInput.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(lineaSubtotal)
                    .build();
            venta.getDetalles().add(detalle);

            producto.setStock(stockActual.subtract(stockADescontar));
            productoRepository.save(producto);
        }

        if (descuento.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("El descuento no puede superar el subtotal");
        }

        BigDecimal total = subtotal.subtract(descuento);
        venta.setSubtotal(subtotal);
        venta.setTotal(total);
        venta = guardar(venta);

        sesion.setTotalVentasPyg(nvl(sesion.getTotalVentasPyg()).add(total));
        sesionCajaRepository.save(sesion);

        Maletin maletin = sesion.getMaletin();
        maletin.setBalancePyg(nvl(maletin.getBalancePyg()).add(total));
        maletinRepository.save(maletin);

        var caja = sesion.getCaja();
        caja.setSaldo_actual(nvl(caja.getSaldo_actual()).add(total));
        cajaRepository.save(caja);

        MovimientoCaja movimiento = MovimientoCaja.builder()
                .caja(caja)
                .tipo("INGRESO")
                .monto(total)
                .concepto("Pago venta " + venta.getNumero() + " - Efectivo")
                .fecha(LocalDateTime.now())
                .persona(sesion.getPersona())
                .moneda("PYG")
                .maletin(maletin)
                .sesionCaja(sesion)
                .referencia(venta.getNumero())
                .build();
        movimiento = movimientoCajaRepository.save(movimiento);

        Ingreso ingreso = Ingreso.builder()
                .movimiento(movimiento)
                .descripcion("Venta POS " + venta.getNumero())
                .origen("VENTA")
                .cliente_o_fuente(cliente != null && cliente.getPersona() != null
                        ? (cliente.getPersona().getNombre() + " " + cliente.getPersona().getApellido()).trim()
                        : "CONSUMIDOR FINAL")
                .observaciones("Efectivo PYG")
                .build();
        ingresoRepository.save(ingreso);

        return mapper.toOutput(venta);
    }

    @Transactional(readOnly = true)
    public VentaOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public List<VentaOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).toList();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
