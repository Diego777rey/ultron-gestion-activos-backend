package com.dev.ultron.service.compras;

import com.dev.ultron.domain.compras.Compra;
import com.dev.ultron.domain.compras.ItemCompra;
import com.dev.ultron.domain.compras.Proveedor;
import com.dev.ultron.domain.compras.Vendedor;
import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.compras.input.CompraInput;
import com.dev.ultron.dto.compras.input.ItemCompraInput;
import com.dev.ultron.dto.compras.mapper.CompraMapper;
import com.dev.ultron.dto.compras.mapper.ItemCompraMapper;
import com.dev.ultron.dto.compras.output.CompraOutput;
import com.dev.ultron.repository.compras.CompraRepository;
import com.dev.ultron.repository.compras.ProveedorRepository;
import com.dev.ultron.repository.compras.VendedorRepository;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.service.financiero.CotizacionService;
import com.dev.ultron.service.operaciones.StockProductoSectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompraService {

    private static final String MONEDA_GUARANI = "PYG";
    /** Mismas formas de pago que el punto de venta. */
    private static final Set<String> FORMAS_PAGO = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");
    private static final Set<String> TIPOS_IVA = Set.of("10", "5", "EXENTA");
    /** Los precios de factura incluyen IVA: IVA 10% = total / 11, IVA 5% = total / 21. */
    private static final BigDecimal DIVISOR_IVA_10 = BigDecimal.valueOf(11);
    private static final BigDecimal DIVISOR_IVA_5 = BigDecimal.valueOf(21);

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final SectorRepository sectorRepository;
    private final CotizacionService cotizacionService;
    private final StockProductoSectorService stockProductoSectorService;
    private final CompraMapper compraMapper;
    private final ItemCompraMapper itemCompraMapper;

    @Transactional(readOnly = true)
    public List<CompraOutput> listarCompras() {
        List<Compra> compras = compraRepository.findAllWithDetails();
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public CompraOutput obtenerCompra(Long id) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
        return compraMapper.toOutput(compra);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorProveedor(Long idProveedor) {
        List<Compra> compras = compraRepository.findByProveedor(idProveedor);
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorEstado(String estado) {
        Compra.EstadoCompra estadoCompra = Compra.EstadoCompra.valueOf(estado.toUpperCase());
        List<Compra> compras = compraRepository.findByEstado(estadoCompra);
        return compraMapper.toOutputList(compras);
    }

    @Transactional(readOnly = true)
    public List<CompraOutput> listarComprasPorRangoFecha(String fechaInicio, String fechaFin) {
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio + " 00:00:00", 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime fin = LocalDateTime.parse(fechaFin + " 23:59:59", 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Compra> compras = compraRepository.findByFechaRange(inicio, fin);
        return compraMapper.toOutputList(compras);
    }

    @Transactional
    public CompraOutput registrarCompra(CompraInput input) {
        Compra compra = compraMapper.toEntity(input);
        compra.setFecha_compra(LocalDateTime.now());
        asignarProveedorYVendedor(compra, input);
        asignarSector(compra, input);
        aplicarCondicionesDePago(compra, input, null, null);
        reemplazarItems(compra, input.items());

        Compra savedCompra = compraRepository.save(compra);
        moverStock(savedCompra, true);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public CompraOutput actualizarCompra(Long id, CompraInput input) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
        String monedaAnterior = compra.getMoneda();
        BigDecimal cotizacionAnterior = compra.getCotizacion();
        moverStock(compra, false);

        compraMapper.updateEntity(compra, input);
        asignarProveedorYVendedor(compra, input);
        asignarSector(compra, input);
        aplicarCondicionesDePago(compra, input, monedaAnterior, cotizacionAnterior);
        reemplazarItems(compra, input.items());

        Compra savedCompra = compraRepository.save(compra);
        moverStock(savedCompra, true);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public CompraOutput cambiarEstadoCompra(Long id, String estado) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));

        Compra.EstadoCompra nuevoEstado = Compra.EstadoCompra.valueOf(estado.toUpperCase());
        if (nuevoEstado == Compra.EstadoCompra.RECIBIDA && compra.getSector() == null) {
            throw new IllegalArgumentException("Indicá el sector donde ingresa la mercadería antes de recibir la compra");
        }
        moverStock(compra, false);
        compra.setEstado(nuevoEstado);
        moverStock(compra, true);

        Compra savedCompra = compraRepository.save(compra);
        return compraMapper.toOutput(savedCompra);
    }

    @Transactional
    public boolean eliminarCompra(Long id) {
        Compra compra = compraRepository.findByIdWithFull(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
        moverStock(compra, false);
        compraRepository.delete(compra);
        return true;
    }

    /**
     * La mercadería de una compra RECIBIDA está en el stock de su sector:
     * {@code ingresar = false} la retira (antes de editar, cancelar o eliminar) y
     * {@code ingresar = true} la vuelve a sumar con los datos actuales.
     */
    private void moverStock(Compra compra, boolean ingresar) {
        if (compra.getEstado() != Compra.EstadoCompra.RECIBIDA || compra.getSector() == null) {
            return;
        }
        Long idSector = compra.getSector().getId_sector();
        for (ItemCompra item : compra.getItems()) {
            BigDecimal cantidad = ingresar ? item.getCantidad() : item.getCantidad().negate();
            stockProductoSectorService.ajustar(item.getProducto().getId_producto(), idSector, cantidad);
        }
    }

    private void asignarSector(Compra compra, CompraInput input) {
        if (input.idSector() == null) {
            if (compra.getEstado() == Compra.EstadoCompra.RECIBIDA) {
                throw new IllegalArgumentException("Seleccioná el sector donde ingresa la mercadería");
            }
            compra.setSector(null);
            return;
        }
        Sector sector = sectorRepository.findById(input.idSector())
            .orElseThrow(() -> new RuntimeException("Sector no encontrado"));
        compra.setSector(sector);
    }

    private void asignarProveedorYVendedor(Compra compra, CompraInput input) {
        Proveedor proveedor = proveedorRepository.findById(input.idProveedor())
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Vendedor vendedor = null;
        if (input.idVendedor() != null) {
            vendedor = vendedorRepository.findById(input.idVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
            if (!vendedor.getProveedor().getId_proveedor().equals(proveedor.getId_proveedor())) {
                throw new IllegalArgumentException("El vendedor no pertenece al proveedor seleccionado");
            }
        }

        compra.setProveedor(proveedor);
        compra.setVendedor(vendedor);
    }

    /**
     * La forma de pago debe ser una de las del sistema y la cotización sale de las cotizaciones
     * activas. Al editar sin cambiar de moneda se conserva la cotización con la que se registró.
     */
    private void aplicarCondicionesDePago(Compra compra, CompraInput input,
                                          String monedaAnterior, BigDecimal cotizacionAnterior) {
        String formaPago = input.formaPago() == null ? "" : input.formaPago().trim().toUpperCase();
        if (!FORMAS_PAGO.contains(formaPago)) {
            throw new IllegalArgumentException("Forma de pago no válida: " + input.formaPago());
        }
        compra.setForma_pago(formaPago);

        String moneda = input.moneda() == null || input.moneda().isBlank()
            ? MONEDA_GUARANI
            : input.moneda().trim();
        if (MONEDA_GUARANI.equalsIgnoreCase(moneda)) {
            compra.setMoneda(MONEDA_GUARANI);
            compra.setCotizacion(BigDecimal.ONE);
            return;
        }
        if (moneda.equalsIgnoreCase(monedaAnterior) && cotizacionAnterior != null) {
            compra.setMoneda(monedaAnterior);
            compra.setCotizacion(cotizacionAnterior);
            return;
        }
        Cotizacion cotizacion = cotizacionService.buscarActiva(moneda);
        if (cotizacion == null) {
            throw new IllegalArgumentException("No hay una cotización activa para la moneda " + moneda);
        }
        compra.setMoneda(cotizacion.getMoneda());
        compra.setCotizacion(cotizacion.getValor());
    }

    private void reemplazarItems(Compra compra, List<ItemCompraInput> itemsInput) {
        if (compra.getItems() == null) {
            compra.setItems(new ArrayList<>());
        }
        compra.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        if (itemsInput != null) {
            for (ItemCompraInput itemInput : itemsInput) {
                Producto producto = productoRepository.findById(itemInput.idProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                if (itemInput.cantidad() == null || itemInput.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("La cantidad de " + producto.getNombre() + " debe ser mayor a cero");
                }
                if (itemInput.precioCosto() == null || itemInput.precioCosto().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("El costo de " + producto.getNombre() + " no puede ser negativo");
                }

                ItemCompra item = itemCompraMapper.toEntity(itemInput);
                item.setCompra(compra);
                item.setProducto(producto);
                item.setTipo_iva(resolverTipoIva(itemInput.tipoIva(), producto));
                calcularLinea(item);

                total = total.add(item.getSubtotal());
                totalIva = totalIva.add(item.getIva());
                compra.getItems().add(item);
            }
        }

        compra.setTotal_iva(totalIva);
        compra.setSubtotal(total.subtract(totalIva));
        compra.setTotal(total);
    }

    private static String resolverTipoIva(String solicitado, Producto producto) {
        String tipo = solicitado != null && !solicitado.isBlank() ? solicitado : producto.getTipoIva();
        String normalizado = tipo == null ? "10" : tipo.trim().toUpperCase();
        return TIPOS_IVA.contains(normalizado) ? normalizado : "10";
    }

    private static void calcularLinea(ItemCompra item) {
        BigDecimal subtotal = item.getCantidad().multiply(item.getPrecio_costo())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = switch (item.getTipo_iva()) {
            case "10" -> subtotal.divide(DIVISOR_IVA_10, 2, RoundingMode.HALF_UP);
            case "5" -> subtotal.divide(DIVISOR_IVA_5, 2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
        item.setSubtotal(subtotal);
        item.setIva(iva);
    }
}
