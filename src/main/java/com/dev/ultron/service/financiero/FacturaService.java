package com.dev.ultron.service.financiero;

import com.dev.ultron.domain.financiero.*;
import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.DetalleFacturaInput;
import com.dev.ultron.dto.financiero.input.FacturaInput;
import com.dev.ultron.dto.financiero.mapper.DetalleFacturaMapper;
import com.dev.ultron.dto.financiero.mapper.FacturaMapper;
import com.dev.ultron.dto.financiero.output.FacturaOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.financiero.FacturaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.financiero.TimbradoRepository;
import com.dev.ultron.repository.financiero.VentaRepository;
import com.dev.ultron.repository.inventario.PresentacionProductoRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.inventario.ServicioRepository;
import com.dev.ultron.repository.personas.ClienteRepository;
import com.dev.ultron.repository.personas.EmpresaRepository;
import com.dev.ultron.service.security.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de facturas legales.
 * Maneja la emisión, anulación y consulta de facturas.
 */
@Service
public class FacturaService extends GenericCrudService<Factura, Long> {

    private static final String IVA_10 = "10";
    private static final BigDecimal DIVISOR_IVA_10 = new BigDecimal("11");
    private static final BigDecimal DIVISOR_IVA_5 = new BigDecimal("21");

    private final FacturaRepository facturaRepository;
    private final TimbradoRepository timbradoRepository;
    private final VentaRepository ventaRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final PresentacionProductoRepository presentacionProductoRepository;
    private final FacturaMapper facturaMapper;
    private final DetalleFacturaMapper detalleFacturaMapper;
    private final AuthService authService;

    public FacturaService(FacturaRepository facturaRepository,
                         TimbradoRepository timbradoRepository,
                         VentaRepository ventaRepository,
                         SesionCajaRepository sesionCajaRepository,
                         ClienteRepository clienteRepository,
                         EmpresaRepository empresaRepository,
                         ProductoRepository productoRepository,
                         ServicioRepository servicioRepository,
                         PresentacionProductoRepository presentacionProductoRepository,
                         FacturaMapper facturaMapper,
                         DetalleFacturaMapper detalleFacturaMapper,
                         AuthService authService) {
        this.facturaRepository = facturaRepository;
        this.timbradoRepository = timbradoRepository;
        this.ventaRepository = ventaRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.presentacionProductoRepository = presentacionProductoRepository;
        this.facturaMapper = facturaMapper;
        this.detalleFacturaMapper = detalleFacturaMapper;
        this.authService = authService;
    }

    @Override
    protected JpaRepository<Factura, Long> getRepository() {
        return facturaRepository;
    }

    /**
     * Emite una nueva factura.
     * Este método es transaccional y maneja la numeración secuencial con lock.
     */
    @Transactional
    public FacturaOutput emitirFactura(FacturaInput input) {
        Timbrado timbrado = obtenerYValidarTimbrado(input.idTimbrado());
        
        Empresa empresa = empresaRepository.findById(input.idEmpresa())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        if (timbrado.getEmpresa() == null
                || !empresa.getId_empresa().equals(timbrado.getEmpresa().getId_empresa())) {
            throw new IllegalArgumentException("El timbrado no pertenece a la empresa registrada");
        }
        
        Cliente cliente = input.idCliente() != null ?
                clienteRepository.findById(input.idCliente()).orElse(null) : null;

        if (input.idVenta() != null && facturaRepository.findByVenta(input.idVenta()).isPresent()) {
            throw new IllegalStateException("Esta venta ya tiene una factura emitida");
        }

        Venta venta = input.idVenta() != null
                ? ventaRepository.findById(input.idVenta())
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"))
                : null;
        SesionCaja sesionCaja = input.idSesionCaja() != null
                ? sesionCajaRepository.findById(input.idSesionCaja())
                    .orElseThrow(() -> new IllegalArgumentException("Sesión de caja no encontrada"))
                : null;
        
        Usuario usuarioEmisor = authService.getAuthenticatedUser();
        
        Factura factura = facturaMapper.toEntity(
                input, timbrado, cliente, venta, sesionCaja, empresa, usuarioEmisor
        );
        
        String numeroFactura = generarNumeroFactura(timbrado);
        factura.setNumero_factura(numeroFactura);
        factura.setTimbrado(timbrado.getNumero_timbrado());
        
        completarDatosCliente(factura, cliente, input);
        
        List<DetalleFactura> detalles = procesarDetalles(input.detalles(), factura);
        factura.setDetalles(detalles);
        
        calcularTotales(factura);
        
        factura = guardar(factura);
        
        return facturaMapper.toOutput(factura);
    }

    /**
     * Genera el número de factura y actualiza el contador del timbrado.
     * Usa lock pesimista para evitar duplicados.
     */
    @Transactional
    private synchronized String generarNumeroFactura(Timbrado timbrado) {
        Integer numeroActual = timbrado.getNumero_actual();
        if (numeroActual == null) {
            throw new IllegalStateException("El timbrado no tiene definido el próximo número");
        }
        
        if (numeroActual > timbrado.getNumero_final()) {
            throw new IllegalStateException(
                    "El timbrado ha alcanzado el límite de numeración. " +
                    "Números disponibles: " + timbrado.getNumero_inicial() +
                    " al " + timbrado.getNumero_final()
            );
        }
        
        String numeroFormateado = String.format(
                "%s-%s-%07d",
                timbrado.getEstablecimiento(),
                timbrado.getPunto_expedicion(),
                numeroActual
        );
        
        timbrado.setNumero_actual(numeroActual + 1);
        timbradoRepository.save(timbrado);
        
        return numeroFormateado;
    }

    /**
     * Obtiene y valida que el timbrado esté activo y vigente.
     */
    private Timbrado obtenerYValidarTimbrado(Long idTimbrado) {
        Timbrado timbrado = timbradoRepository.findById(idTimbrado)
                .orElseThrow(() -> new IllegalArgumentException("Timbrado no encontrado"));
        
        if (!Boolean.TRUE.equals(timbrado.getActivo())) {
            throw new IllegalStateException("El timbrado no está activo");
        }
        if (timbrado.getTipo_factura() != null && !"PAPEL".equalsIgnoreCase(timbrado.getTipo_factura())) {
            throw new IllegalStateException(
                    "La facturación electrónica todavía no está habilitada. Usá un timbrado de factura en papel"
            );
        }
        if (timbrado.getNumero_actual() == null) {
            throw new IllegalStateException("El timbrado no tiene definido el próximo número");
        }
        
        LocalDateTime hoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime inicio = timbrado.getFecha_inicio_vigencia().atStartOfDay();
        LocalDateTime fin = timbrado.getFecha_fin_vigencia().atTime(23, 59, 59);
        
        if (hoy.isBefore(inicio) || hoy.isAfter(fin)) {
            throw new IllegalStateException(
                    "El timbrado no está vigente. " +
                    "Vigencia: " + timbrado.getFecha_inicio_vigencia() +
                    " al " + timbrado.getFecha_fin_vigencia()
            );
        }
        
        return timbrado;
    }

    /**
     * Completa los datos del cliente en la factura (snapshot).
     */
    private void completarDatosCliente(Factura factura, Cliente cliente, FacturaInput input) {
        if (cliente != null && cliente.getPersona() != null) {
            String nombre = unir(cliente.getPersona().getNombre(), cliente.getPersona().getApellido());
            factura.setCliente_nombre(nombre.isBlank() ? "SIN NOMBRE" : nombre);
            factura.setCliente_documento(cliente.getPersona().getDocumento());
            factura.setCliente_ruc(cliente.getRuc() != null ? cliente.getRuc() : cliente.getPersona().getDocumento());
            factura.setCliente_direccion(cliente.getPersona().getDireccion());
        } else {
            String nombre = input.clienteNombre();
            factura.setCliente_nombre(nombre == null || nombre.isBlank() ? "SIN NOMBRE" : nombre);
            factura.setCliente_documento(input.clienteDocumento());
            factura.setCliente_ruc(input.clienteRuc());
            factura.setCliente_direccion(input.clienteDireccion());
        }
    }

    /**
     * Procesa los detalles de la factura.
     */
    private List<DetalleFactura> procesarDetalles(List<DetalleFacturaInput> detallesInput, Factura factura) {
        if (detallesInput == null || detallesInput.isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un detalle");
        }
        
        List<DetalleFactura> detalles = new ArrayList<>();
        int numeroLinea = 1;
        
        for (DetalleFacturaInput detalleInput : detallesInput) {
            Producto producto = detalleInput.idProducto() != null ?
                    productoRepository.findById(detalleInput.idProducto()).orElse(null) : null;
            
            Servicio servicio = detalleInput.idServicio() != null ?
                    servicioRepository.findById(detalleInput.idServicio()).orElse(null) : null;
            
            PresentacionProducto presentacion = detalleInput.idPresentacion() != null ?
                    presentacionProductoRepository.findById(detalleInput.idPresentacion()).orElse(null) : null;
            
            DetalleFactura detalle = detalleFacturaMapper.toEntity(
                    detalleInput, factura, producto, servicio, presentacion, numeroLinea
            );
            
            if (detalle.getDescripcion() == null || detalle.getDescripcion().isEmpty()) {
                if (producto != null) {
                    detalle.setDescripcion(producto.getNombre());
                    detalle.setCodigo(producto.getCodigo());
                } else if (servicio != null) {
                    detalle.setDescripcion(servicio.getNombre());
                    detalle.setCodigo(servicio.getCodigo());
                }
            }
            
            calcularTotalesDetalle(detalle);
            
            detalles.add(detalle);
            numeroLinea++;
        }
        
        return detalles;
    }

    /**
     * Calcula los totales de una línea de detalle según el tipo de IVA.
     */
    private void calcularTotalesDetalle(DetalleFactura detalle) {
        BigDecimal subtotal = detalle.getCantidad().multiply(detalle.getPrecio_unitario())
                .setScale(0, RoundingMode.HALF_UP);
        detalle.setSubtotal(subtotal);
        
        BigDecimal montoIva = BigDecimal.ZERO;
        String tipoIva = detalle.getTipo_iva();
        
        if ("5".equals(tipoIva)) {
            montoIva = subtotal.divide(DIVISOR_IVA_5, 0, RoundingMode.HALF_UP);
        } else if ("10".equals(tipoIva)) {
            montoIva = subtotal.divide(DIVISOR_IVA_10, 0, RoundingMode.HALF_UP);
        }
        
        detalle.setMonto_iva(montoIva);
        detalle.setTotal_linea(subtotal);
    }

    /**
     * Calcula los totales de la factura.
     */
    private void calcularTotales(Factura factura) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalIva5 = BigDecimal.ZERO;
        BigDecimal totalIva10 = BigDecimal.ZERO;
        BigDecimal totalExenta = BigDecimal.ZERO;
        
        for (DetalleFactura detalle : factura.getDetalles()) {
            subtotal = subtotal.add(detalle.getSubtotal());
            
            String tipoIva = detalle.getTipo_iva();
            if ("5".equals(tipoIva)) {
                totalIva5 = totalIva5.add(detalle.getMonto_iva());
            } else if ("10".equals(tipoIva)) {
                totalIva10 = totalIva10.add(detalle.getMonto_iva());
            } else if ("EXENTA".equals(tipoIva)) {
                totalExenta = totalExenta.add(detalle.getSubtotal());
            }
        }
        
        BigDecimal totalIva = totalIva5.add(totalIva10);
        
        factura.setSubtotal(subtotal);
        factura.setTotal_iva_5(totalIva5);
        factura.setTotal_iva_10(totalIva10);
        factura.setTotal_exenta(totalExenta);
        factura.setTotal_iva(totalIva);
        factura.setTotal(subtotal);
    }

    /**
     * Anula una factura existente.
     */
    @Transactional
    public FacturaOutput anularFactura(Long id, String motivo) {
        Factura factura = buscarPorIdOrThrow(id);
        
        if ("ANULADA".equals(factura.getEstado())) {
            throw new IllegalStateException("La factura ya está anulada");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio");
        }
        
        Usuario usuarioAnulacion = authService.getAuthenticatedUser();
        
        factura.setEstado("ANULADA");
        factura.setFecha_anulacion(LocalDateTime.now());
        factura.setMotivo_anulacion(motivo.toUpperCase());
        factura.setUsuario_anulacion(usuarioAnulacion);
        
        factura = actualizar(factura);
        
        return facturaMapper.toOutput(factura);
    }

    /**
     * Obtiene una factura por ID con todos sus detalles.
     */
    @Transactional(readOnly = true)
    public FacturaOutput obtenerPorIdConDetalles(Long id) {
        Factura factura = facturaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        return facturaMapper.toOutput(factura);
    }

    /**
     * Busca facturas por empresa y estado.
     */
    @Transactional(readOnly = true)
    public Page<FacturaOutput> buscarPorEmpresaYEstado(Long idEmpresa, String estado, Pageable pageable) {
        Page<Factura> facturas = facturaRepository.findByEmpresaAndEstado(idEmpresa, estado, pageable);
        return facturas.map(facturaMapper::toOutput);
    }

    /**
     * Lista facturas legales con venta asociada por empresa y estado.
     * Útil para el listado de facturas emitidas desde el punto de venta.
     */
    @Transactional(readOnly = true)
    public Page<FacturaOutput> listarFacturasConVentaPorEmpresaYEstado(Long idEmpresa, String estado, Pageable pageable) {
        Page<Factura> facturas = facturaRepository.findFacturasConVentaPorEmpresaYEstado(idEmpresa, estado, pageable);
        return facturas.map(facturaMapper::toOutput);
    }

    /**
     * Busca facturas con un filtro de texto.
     */
    @Transactional(readOnly = true)
    public Page<FacturaOutput> buscarConFiltro(Long idEmpresa, String filtro, Pageable pageable) {
        Page<Factura> facturas = facturaRepository.searchByEmpresa(idEmpresa, filtro, pageable);
        return facturas.map(facturaMapper::toOutput);
    }

    /**
     * Busca facturas con venta asociada usando un filtro de texto.
     * Busca en número de factura, nombre del cliente, RUC y documento.
     */
    @Transactional(readOnly = true)
    public Page<FacturaOutput> buscarFacturasConVentaConFiltro(Long idEmpresa, String filtro, Pageable pageable) {
        Page<Factura> facturas = facturaRepository.searchFacturasConVentaByEmpresa(idEmpresa, filtro, pageable);
        return facturas.map(facturaMapper::toOutput);
    }

    /**
     * Obtiene facturas de un cliente.
     */
    @Transactional(readOnly = true)
    public Page<FacturaOutput> obtenerPorCliente(Long idCliente, Pageable pageable) {
        Page<Factura> facturas = facturaRepository.findByCliente(idCliente, pageable);
        return facturas.map(facturaMapper::toOutput);
    }

    /**
     * Obtiene facturas para reporte de libro de ventas.
     */
    @Transactional(readOnly = true)
    public List<FacturaOutput> obtenerParaLibroVentas(Long idEmpresa, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Factura> facturas = facturaRepository.findParaReporteLibroVentas(idEmpresa, fechaInicio, fechaFin);
        return facturas.stream()
                .map(facturaMapper::toOutput)
                .collect(Collectors.toList());
    }

    /**
     * Emite la factura en papel de una venta ya cobrada, usando la empresa
     * registrada y el timbrado activo, vigente y con próximo número.
     */
    @Transactional
    public FacturaOutput emitirDesdeVenta(Venta venta) {
        if (venta == null || venta.getId_venta() == null) {
            throw new IllegalArgumentException("La venta es obligatoria para emitir la factura");
        }
        Empresa empresa = resolverEmpresaRegistrada();
        Timbrado timbrado = timbradoRepository.findTimbradoPapelDisponible(empresa.getId_empresa())
                .orElseThrow(() -> new IllegalStateException(
                        "No hay un timbrado de factura en papel activo, vigente y con el próximo número definido. "
                                + "Cargalo en Timbrados"
                ));
        Long idCliente = venta.getCliente() != null ? venta.getCliente().getId_cliente() : null;
        Long idSesion = venta.getSesionCaja() != null ? venta.getSesionCaja().getId_sesion_caja() : null;
        String formaPago = venta.getFormaPago() != null ? venta.getFormaPago() : "EFECTIVO";
        FacturaInput input = new FacturaInput(
                timbrado.getId_timbrado(),
                idCliente,
                venta.getId_venta(),
                idSesion,
                empresa.getId_empresa(),
                null,
                null,
                null,
                null,
                formaPago,
                "PYG",
                null,
                detallesDesdeVenta(venta)
        );
        return emitirFactura(input);
    }

    @Transactional(readOnly = true)
    public FacturaOutput obtenerPorVenta(Long idVenta) {
        if (idVenta == null) {
            return null;
        }
        return facturaRepository.findByVentaConEmisor(idVenta)
                .map(factura -> {
                    factura.getDetalles().size();
                    return facturaMapper.toOutput(factura);
                })
                .orElse(null);
    }

    private Empresa resolverEmpresaRegistrada() {
        return empresaRepository.findAll().stream()
                .filter(empresa -> !Boolean.FALSE.equals(empresa.getActiva()))
                .min(Comparator.comparing(Empresa::getId_empresa))
                .orElseThrow(() -> new IllegalStateException(
                        "No hay una empresa registrada. Cargala en Datos de facturación"
                ));
    }

    private List<DetalleFacturaInput> detallesDesdeVenta(Venta venta) {
        List<DetalleVenta> lineas = venta.getDetalles();
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene ítems para facturar");
        }
        BigDecimal descuento = nvl(venta.getDescuento()).setScale(0, RoundingMode.HALF_UP);
        BigDecimal base = lineas.stream()
                .map(detalle -> nvl(detalle.getSubtotal()).setScale(0, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal objetivo = base.subtract(descuento);
        if (objetivo.signum() < 0) {
            throw new IllegalArgumentException("El descuento no puede superar el total de la factura");
        }

        List<DetalleFacturaInput> detalles = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 0; i < lineas.size(); i++) {
            DetalleVenta detalle = lineas.get(i);
            BigDecimal bruto = nvl(detalle.getSubtotal()).setScale(0, RoundingMode.HALF_UP);
            BigDecimal neto = i == lineas.size() - 1
                    ? objetivo.subtract(acumulado)
                    : (base.signum() == 0
                        ? BigDecimal.ZERO
                        : bruto.multiply(objetivo).divide(base, 0, RoundingMode.HALF_UP));
            if (neto.signum() < 0) {
                neto = BigDecimal.ZERO;
            }
            if (i < lineas.size() - 1) {
                acumulado = acumulado.add(neto);
            }

            BigDecimal cantidad = nvl(detalle.getCantidad());
            if (cantidad.signum() <= 0) {
                cantidad = BigDecimal.ONE;
            }
            BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : neto;
            String descripcion = descripcionLinea(detalle);
            if (descuento.signum() > 0) {
                BigDecimal producido = precioEntero(cantidad, neto).multiply(cantidad).setScale(0, RoundingMode.HALF_UP);
                if (producido.compareTo(neto) == 0) {
                    precio = precioEntero(cantidad, neto);
                } else {
                    descripcion = cantidad.stripTrailingZeros().toPlainString() + " x " + descripcion;
                    cantidad = BigDecimal.ONE;
                    precio = neto;
                }
            }
            detalles.add(new DetalleFacturaInput(
                    detalle.getProducto() != null ? detalle.getProducto().getId_producto() : null,
                    detalle.getServicio() != null ? detalle.getServicio().getId_servicio() : null,
                    detalle.getPresentacion() != null ? detalle.getPresentacion().getId_presentacion_producto() : null,
                    descripcion,
                    detalle.getProducto() != null ? detalle.getProducto().getCodigo() : null,
                    cantidad,
                    precio,
                    tipoIvaDe(detalle)
            ));
        }
        return detalles;
    }

    private static BigDecimal precioEntero(BigDecimal cantidad, BigDecimal neto) {
        return neto.divide(cantidad, 0, RoundingMode.HALF_UP);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String tipoIvaDe(DetalleVenta detalle) {
        if (detalle.getProducto() == null || detalle.getProducto().getTipoIva() == null
                || detalle.getProducto().getTipoIva().isBlank()) {
            return IVA_10;
        }
        String valor = detalle.getProducto().getTipoIva().trim().toUpperCase();
        if ("5".equals(valor) || "10".equals(valor) || "EXENTA".equals(valor)) {
            return valor;
        }
        return IVA_10;
    }

    private static String descripcionLinea(DetalleVenta detalle) {
        if (detalle.getDescripcion() != null && !detalle.getDescripcion().isBlank()) {
            return detalle.getDescripcion();
        }
        if (detalle.getProducto() != null) {
            String nombre = detalle.getProducto().getNombre() != null ? detalle.getProducto().getNombre() : "ITEM";
            if (detalle.getPresentacion() != null && detalle.getPresentacion().getDescripcion() != null) {
                return nombre + " " + detalle.getPresentacion().getDescripcion();
            }
            return nombre;
        }
        if (detalle.getServicio() != null && detalle.getServicio().getNombre() != null) {
            return detalle.getServicio().getNombre();
        }
        if (detalle.getOrdenTrabajo() != null && detalle.getOrdenTrabajo().getNumeroOrden() != null) {
            return "ORDEN " + detalle.getOrdenTrabajo().getNumeroOrden();
        }
        return "ITEM";
    }

    private static String unir(String nombre, String apellido) {
        String primero = nombre != null ? nombre.trim() : "";
        String segundo = apellido != null ? apellido.trim() : "";
        return (primero + " " + segundo).trim();
    }
}
