package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.DetalleVenta;
import com.dev.ultron.domain.financiero.Ingreso;
import com.dev.ultron.domain.financiero.MovimientoCaja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.financiero.Venta;
import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.financiero.input.DetalleVentaInput;
import com.dev.ultron.dto.financiero.input.VentaInput;
import com.dev.ultron.dto.financiero.mapper.VentaMapper;
import com.dev.ultron.dto.financiero.output.VentaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.IngresoRepository;
import com.dev.ultron.repository.financiero.MovimientoCajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.inventario.ServicioRepository;
import com.dev.ultron.repository.personas.ClienteRepository;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import com.dev.ultron.service.operaciones.StockProductoSectorService;
import com.dev.ultron.service.taller.orden.OrdenTrabajoFlujoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VentaService extends GenericCrudService<Venta, Long> {

    private final VentaRepository repository;
    private final VentaMapper mapper;
    private final SesionCajaRepository sesionCajaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final IngresoRepository ingresoRepository;
    private final CajaRepository cajaRepository;
    private final StockProductoSectorService stockProductoSectorService;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoFlujoService ordenTrabajoFlujoService;

    public VentaService(
            VentaRepository repository,
            VentaMapper mapper,
            SesionCajaRepository sesionCajaRepository,
            ProductoRepository productoRepository,
            ServicioRepository servicioRepository,
            ClienteRepository clienteRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            IngresoRepository ingresoRepository,
            CajaRepository cajaRepository,
            StockProductoSectorService stockProductoSectorService,
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoFlujoService ordenTrabajoFlujoService) {
        this.repository = repository;
        this.mapper = mapper;
        this.sesionCajaRepository = sesionCajaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.ingresoRepository = ingresoRepository;
        this.cajaRepository = cajaRepository;
        this.stockProductoSectorService = stockProductoSectorService;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoFlujoService = ordenTrabajoFlujoService;
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
        if (sesion.getCaja() == null || sesion.getCaja().getSector() == null
                || sesion.getCaja().getSector().getId_sector() == null) {
            throw new IllegalArgumentException("La caja de la sesión no tiene sector asignado");
        }
        Long idSectorCaja = sesion.getCaja().getSector().getId_sector();

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

        String formaPago = input.getFormaPago() != null && !input.getFormaPago().isBlank()
                ? input.getFormaPago().toUpperCase()
                : "EFECTIVO";
        if (!formaPago.equals("EFECTIVO") && !formaPago.equals("TARJETA") && !formaPago.equals("TRANSFERENCIA")) {
            formaPago = "EFECTIVO";
        }

        Venta venta = Venta.builder()
                .numero(numero)
                .fecha(LocalDateTime.now())
                .sesionCaja(sesion)
                .cliente(cliente)
                .descuento(descuento)
                .estado("PAGADA")
                .formaPago(formaPago)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        Set<Long> ordenesAFacturar = new LinkedHashSet<>();
        for (DetalleVentaInput detInput : input.getDetalles()) {
            if (detInput.getIdOrdenTrabajo() != null) {
                BigDecimal lineaSubtotal = agregarDetalleOrdenTrabajo(venta, detInput, ordenesAFacturar);
                subtotal = subtotal.add(lineaSubtotal);
                if (cliente == null && venta.getCliente() != null) {
                    cliente = venta.getCliente();
                }
                continue;
            }

            if (detInput.getIdServicio() != null) {
                subtotal = subtotal.add(agregarDetalleServicio(venta, detInput));
                continue;
            }

            if (detInput.getIdProducto() == null || detInput.getCantidad() == null
                    || detInput.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto y cantidad válida");
            }

            Producto producto = productoRepository.findById(detInput.getIdProducto())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + detInput.getIdProducto()));

            PresentacionProducto presentacion = resolverPresentacion(producto, detInput.getIdPresentacion());
            BigDecimal precio = precioDeVenta(producto, presentacion, detInput.getPrecioUnitario());
            BigDecimal unidadesPorPresentacion = unidadesPorPresentacion(presentacion);
            BigDecimal stockADescontar = detInput.getCantidad().multiply(unidadesPorPresentacion);

            BigDecimal lineaSubtotal = precio.multiply(detInput.getCantidad());
            subtotal = subtotal.add(lineaSubtotal);

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .presentacion(presentacion)
                    .descripcion(presentacion != null ? presentacion.getDescripcion() : null)
                    .cantidad(detInput.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(lineaSubtotal)
                    .build();
            venta.getDetalles().add(detalle);

            stockProductoSectorService.ajustar(
                    producto.getId_producto(),
                    idSectorCaja,
                    stockADescontar.negate()
            );
        }

        if (descuento.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("El descuento no puede superar el subtotal");
        }

        BigDecimal total = subtotal.subtract(descuento);
        venta.setSubtotal(subtotal);
        venta.setTotal(total);
        venta = guardar(venta);

        for (Long idOrden : ordenesAFacturar) {
            ordenTrabajoFlujoService.marcarFacturada(idOrden);
        }

        sesion.setTotalVentasPyg(nvl(sesion.getTotalVentasPyg()).add(total));
        sesionCajaRepository.save(sesion);

        var caja = sesion.getCaja();
        caja.setSaldo_actual(nvl(caja.getSaldo_actual()).add(total));
        cajaRepository.save(caja);

        String etiquetaFormaPago = switch (formaPago) {
            case "TARJETA" -> "Tarjeta";
            case "TRANSFERENCIA" -> "Transferencia";
            default -> "Efectivo";
        };

        MovimientoCaja movimiento = MovimientoCaja.builder()
                .caja(caja)
                .tipo("INGRESO")
                .monto(total)
                .concepto("Pago venta " + venta.getNumero() + " - " + etiquetaFormaPago)
                .fecha(LocalDateTime.now())
                .persona(sesion.getPersona())
                .moneda("PYG")
                .maletin(sesion.getMaletin())
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
                .observaciones(etiquetaFormaPago + " PYG")
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
        return findAllPaginated(page, size, filter, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaOutput> findAllPaginated(int page, int size, String filter, Long idSesionCaja) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        if (idSesionCaja != null) {
            return new PageResponse<>(
                    repository.buscarPorSesion(idSesionCaja, filter, pageable).map(mapper::toOutput));
        }
        return new PageResponse<>(repository.buscar(filter, pageable).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public List<VentaOutput> findAll() {
        return listarTodos().stream().map(mapper::toOutput).toList();
    }

    private BigDecimal agregarDetalleServicio(Venta venta, DetalleVentaInput detInput) {
        if (detInput.getCantidad() == null || detInput.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El servicio debe tener una cantidad válida");
        }

        Servicio servicio = servicioRepository.findById(detInput.getIdServicio())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Servicio no encontrado con id: " + detInput.getIdServicio()));
        if (!servicio.isEstado()) {
            throw new IllegalArgumentException("El servicio " + servicio.getNombre() + " no está activo");
        }

        BigDecimal precio = detInput.getPrecioUnitario() != null
                ? detInput.getPrecioUnitario()
                : servicio.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El servicio " + servicio.getNombre() + " no tiene un precio válido");
        }

        BigDecimal lineaSubtotal = precio.multiply(detInput.getCantidad());
        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .servicio(servicio)
                .descripcion(servicio.getNombre())
                .cantidad(detInput.getCantidad())
                .precioUnitario(precio)
                .subtotal(lineaSubtotal)
                .build();
        venta.getDetalles().add(detalle);
        return lineaSubtotal;
    }

    private BigDecimal agregarDetalleOrdenTrabajo(
            Venta venta,
            DetalleVentaInput detInput,
            Set<Long> ordenesAFacturar) {
        Long idOrden = detInput.getIdOrdenTrabajo();
        if (!ordenesAFacturar.add(idOrden)) {
            throw new IllegalArgumentException("La misma orden de trabajo no puede cobrarse dos veces en la venta");
        }

        OrdenTrabajo orden = ordenTrabajoRepository.findById(idOrden)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada con id: " + idOrden));
        if (!"FINALIZADA".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se pueden cobrar órdenes FINALIZADAS. "
                            + orden.getNumeroOrden() + " está en " + orden.getEtapa());
        }

        BigDecimal precio = totalOrden(orden);
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La orden " + orden.getNumeroOrden() + " no tiene un monto para cobrar");
        }

        if (venta.getCliente() == null && orden.getCliente() != null) {
            venta.setCliente(orden.getCliente());
        }

        String descripcion = detInput.getDescripcion() != null && !detInput.getDescripcion().isBlank()
                ? detInput.getDescripcion()
                : descripcionOrden(orden);

        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .ordenTrabajo(orden)
                .descripcion(descripcion)
                .cantidad(BigDecimal.ONE)
                .precioUnitario(precio)
                .subtotal(precio)
                .build();
        venta.getDetalles().add(detalle);
        return precio;
    }

    private BigDecimal totalOrden(OrdenTrabajo orden) {
        if (orden.getDiagnostico() != null && orden.getDiagnostico().getTotalPresupuesto() != null
                && orden.getDiagnostico().getTotalPresupuesto().compareTo(BigDecimal.ZERO) > 0) {
            return orden.getDiagnostico().getTotalPresupuesto();
        }
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orden.getDetalles().stream()
                .map(OrdenTrabajoDetalle::getSubtotal)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String descripcionOrden(OrdenTrabajo orden) {
        String numero = orden.getNumeroOrden() != null ? orden.getNumeroOrden() : "OT";
        if (orden.getVehiculo() == null) {
            return numero;
        }
        String chapa = orden.getVehiculo().getChapa();
        if (chapa == null || chapa.isBlank()) {
            return numero;
        }
        return numero + " · " + chapa;
    }

    /**
     * Si el producto tiene presentaciones, la venta usa una de ellas.
     * Sin id, toma la primera (la principal). Sin presentaciones, vende el producto suelto.
     */
    private PresentacionProducto resolverPresentacion(Producto producto, Long idPresentacion) {
        List<PresentacionProducto> presentaciones = producto.getPresentaciones() == null
                ? List.of()
                : producto.getPresentaciones();
        if (presentaciones.isEmpty()) {
            if (idPresentacion != null) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene presentaciones");
            }
            return null;
        }
        if (idPresentacion == null) {
            return presentaciones.get(0);
        }
        for (PresentacionProducto presentacion : presentaciones) {
            if (idPresentacion.equals(presentacion.getId_presentacion_producto())) {
                return presentacion;
            }
        }
        throw new IllegalArgumentException(
                "La presentación no pertenece al producto " + producto.getNombre());
    }

    private BigDecimal precioDeVenta(Producto producto, PresentacionProducto presentacion, BigDecimal precioInformado) {
        BigDecimal precio = presentacion != null ? presentacion.getPrecio() : precioInformado;
        if (precio == null) {
            precio = producto.getPrecioVenta();
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene un precio de venta válido");
        }
        return precio;
    }

    private BigDecimal unidadesPorPresentacion(PresentacionProducto presentacion) {
        if (presentacion == null || presentacion.getCantidad() == null
                || presentacion.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return presentacion.getCantidad();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
