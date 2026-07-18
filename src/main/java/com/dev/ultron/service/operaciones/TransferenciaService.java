package com.dev.ultron.service.operaciones;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.operaciones.TransferenciaDetalle;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.operaciones.input.TransferenciaDetalleInput;
import com.dev.ultron.dto.operaciones.input.TransferenciaInput;
import com.dev.ultron.dto.operaciones.mapper.TransferenciaMapper;
import com.dev.ultron.dto.operaciones.output.TransferenciaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.operaciones.TransferenciaRepository;
import com.dev.ultron.repository.personas.PersonaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
public class TransferenciaService extends GenericCrudService<Transferencia, Long> {

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONFERIDO = "CONFERIDO";
    public static final String ESTADO_RECEPCIONADO = "RECEPCIONADO";

    private final TransferenciaRepository repository;
    private final TransferenciaMapper mapper;
    private final SectorRepository sectorRepository;
    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;
    private final StockProductoSectorService stockService;

    public TransferenciaService(
            TransferenciaRepository repository,
            TransferenciaMapper mapper,
            SectorRepository sectorRepository,
            ProductoRepository productoRepository,
            PersonaRepository personaRepository,
            StockProductoSectorService stockService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.sectorRepository = sectorRepository;
        this.productoRepository = productoRepository;
        this.personaRepository = personaRepository;
        this.stockService = stockService;
    }

    @Override
    protected JpaRepository<Transferencia, Long> getRepository() {
        return repository;
    }

    /**
     * Crea solo la cabecera (origen/destino) en estado PENDIENTE.
     * Los productos se cargan después en la pantalla de gestión.
     */
    @Transactional
    public TransferenciaOutput registrar(TransferenciaInput input) {
        if (input.getIdSectorOrigen() == null || input.getIdSectorDestino() == null) {
            throw new IllegalArgumentException("Debe indicar sector origen y destino");
        }
        if (input.getIdSectorOrigen().equals(input.getIdSectorDestino())) {
            throw new IllegalArgumentException("El sector origen y destino deben ser distintos");
        }

        Sector origen = sectorRepository.findById(input.getIdSectorOrigen())
                .orElseThrow(() -> new EntityNotFoundException("Sector origen no encontrado"));
        Sector destino = sectorRepository.findById(input.getIdSectorDestino())
                .orElseThrow(() -> new EntityNotFoundException("Sector destino no encontrado"));

        Persona persona = null;
        if (input.getIdPersona() != null) {
            persona = personaRepository.findById(input.getIdPersona())
                    .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + input.getIdPersona()));
        }

        Transferencia transferencia = Transferencia.builder()
                .numero(generarNumero())
                .sectorOrigen(origen)
                .sectorDestino(destino)
                .estado(ESTADO_PENDIENTE)
                .fecha(LocalDateTime.now())
                .persona(persona)
                .build();

        return mapper.toOutput(guardar(transferencia));
    }

    /**
     * Agrega un producto a una transferencia PENDIENTE y descuenta stock del origen.
     */
    @Transactional
    public TransferenciaOutput agregarProducto(Long idTransferencia, TransferenciaDetalleInput input) {
        Transferencia transferencia = buscarPorIdOrThrow(idTransferencia);
        if (!ESTADO_PENDIENTE.equalsIgnoreCase(transferencia.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden agregar productos a transferencias PENDIENTES");
        }
        if (input == null || input.getIdProducto() == null) {
            throw new IllegalArgumentException("Debe indicar el producto");
        }
        if (input.getCantidad() == null || input.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        boolean yaExiste = transferencia.getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null
                        && input.getIdProducto().equals(d.getProducto().getId_producto()));
        if (yaExiste) {
            throw new IllegalArgumentException("El producto ya está en la transferencia");
        }

        Producto producto = productoRepository.findById(input.getIdProducto())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + input.getIdProducto()));

        stockService.ajustar(
                producto.getId_producto(),
                transferencia.getSectorOrigen().getId_sector(),
                input.getCantidad().negate()
        );

        TransferenciaDetalle detalle = TransferenciaDetalle.builder()
                .transferencia(transferencia)
                .producto(producto)
                .cantidad(input.getCantidad())
                .build();
        transferencia.getDetalles().add(detalle);

        Transferencia guardada = actualizar(transferencia);
        stockService.resyncProductoStock(producto.getId_producto());
        return mapper.toOutput(guardada);
    }

    /**
     * Quita un producto de una transferencia PENDIENTE y devuelve el stock al origen.
     */
    @Transactional
    public TransferenciaOutput eliminarProducto(Long idTransferencia, Long idDetalle) {
        Transferencia transferencia = buscarPorIdOrThrow(idTransferencia);
        if (!ESTADO_PENDIENTE.equalsIgnoreCase(transferencia.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden quitar productos de transferencias PENDIENTES");
        }

        TransferenciaDetalle detalle = transferencia.getDetalles().stream()
                .filter(d -> idDetalle.equals(d.getId_detalle()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado con id: " + idDetalle));

        Producto producto = detalle.getProducto();
        BigDecimal cantidad = detalle.getCantidad();
        stockService.ajustar(
                producto.getId_producto(),
                transferencia.getSectorOrigen().getId_sector(),
                cantidad
        );

        boolean removed = transferencia.getDetalles().removeIf(d -> idDetalle.equals(d.getId_detalle()));
        if (!removed) {
            throw new EntityNotFoundException("Detalle no encontrado con id: " + idDetalle);
        }
        Transferencia guardada = actualizar(transferencia);
        stockService.resyncProductoStock(producto.getId_producto());
        return mapper.toOutput(guardada);
    }

    @Transactional
    public TransferenciaOutput conferir(Long id) {
        Transferencia transferencia = buscarPorIdOrThrow(id);
        if (!ESTADO_PENDIENTE.equalsIgnoreCase(transferencia.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede conferir una transferencia en estado PENDIENTE. Estado actual: "
                            + transferencia.getEstado()
            );
        }
        if (transferencia.getDetalles() == null || transferencia.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe cargar al menos un producto antes de conferir");
        }
        transferencia.setEstado(ESTADO_CONFERIDO);
        return mapper.toOutput(actualizar(transferencia));
    }

    @Transactional
    public TransferenciaOutput recepcionar(Long id) {
        Transferencia transferencia = buscarPorIdOrThrow(id);
        if (!ESTADO_CONFERIDO.equalsIgnoreCase(transferencia.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede recepcionar una transferencia en estado CONFERIDO. Estado actual: "
                            + transferencia.getEstado()
            );
        }
        if (transferencia.getDetalles() == null || transferencia.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La transferencia no tiene productos para recepcionar");
        }

        Sector destino = transferencia.getSectorDestino();
        Set<Long> productosAfectados = new HashSet<>();
        for (TransferenciaDetalle detalle : transferencia.getDetalles()) {
            Producto producto = detalle.getProducto();
            stockService.ajustar(producto.getId_producto(), destino.getId_sector(), detalle.getCantidad());
            productosAfectados.add(producto.getId_producto());
        }

        transferencia.setEstado(ESTADO_RECEPCIONADO);
        Transferencia actualizada = actualizar(transferencia);
        for (Long idProducto : productosAfectados) {
            stockService.resyncProductoStock(idProducto);
        }
        return mapper.toOutput(actualizada);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferenciaOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public TransferenciaOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private String generarNumero() {
        String prefix = "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long seq = repository.countByNumeroStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
