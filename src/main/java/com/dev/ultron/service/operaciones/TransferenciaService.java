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
import java.util.Locale;
import java.util.Set;

@Service
public class TransferenciaService extends GenericCrudService<Transferencia, Long> {

    /** Carga de productos (origen/destino ya definidos). */
    public static final String ESTADO_CREACION = "CREACION";
    /** Productos cargados: aceptar/rechazar cada ítem. */
    public static final String ESTADO_PENDIENTE_CONFERIR = "PENDIENTE_CONFERIR";
    public static final String ESTADO_CONFERIDO = "CONFERIDO";
    public static final String ESTADO_RECEPCIONADO = "RECEPCIONADO";

    /** Alias legado (migrado a CREACION). */
    public static final String ESTADO_PENDIENTE_LEGADO = "PENDIENTE";

    public static final String DETALLE_PENDIENTE = "PENDIENTE";
    public static final String DETALLE_VERIFICADO = "VERIFICADO";
    public static final String DETALLE_RECHAZADO = "RECHAZADO";

    public static final String MOTIVO_AVERIADO = "AVERIADO";
    public static final String MOTIVO_VENCIDO = "VENCIDO";
    public static final String MOTIVO_ENVIADO_MAL = "ENVIADO_MAL";
    public static final String MOTIVO_OTRO = "OTRO";

    private static final Set<String> MOTIVOS_RECHAZO = Set.of(
            MOTIVO_AVERIADO,
            MOTIVO_VENCIDO,
            MOTIVO_ENVIADO_MAL,
            MOTIVO_OTRO
    );

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
     * Crea solo la cabecera (origen/destino) en etapa CREACION.
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
                .estado(ESTADO_CREACION)
                .fecha(LocalDateTime.now())
                .persona(persona)
                .build();

        return mapper.toOutput(guardar(transferencia));
    }

    /**
     * Agrega un producto solo en etapa CREACION y descuenta stock del origen.
     */
    @Transactional
    public TransferenciaOutput agregarProducto(Long idTransferencia, TransferenciaDetalleInput input) {
        Transferencia transferencia = buscarPorIdOrThrow(idTransferencia);
        if (!esCreacion(transferencia.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden agregar productos en la etapa de Creación. Estado actual: "
                            + transferencia.getEstado()
            );
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
                .estado(DETALLE_PENDIENTE)
                .build();
        transferencia.getDetalles().add(detalle);

        Transferencia guardada = actualizar(transferencia);
        stockService.resyncProductoStock(producto.getId_producto());
        return mapper.toOutput(guardada);
    }

    /**
     * Quita un producto solo en etapa CREACION y devuelve el stock al origen.
     */
    @Transactional
    public TransferenciaOutput eliminarProducto(Long idTransferencia, Long idDetalle) {
        Transferencia transferencia = buscarPorIdOrThrow(idTransferencia);
        if (!esCreacion(transferencia.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden quitar productos en la etapa de Creación. Estado actual: "
                            + transferencia.getEstado()
            );
        }

        TransferenciaDetalle detalle = encontrarDetalle(transferencia, idDetalle);
        Producto producto = detalle.getProducto();
        BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;

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

    /**
     * Marca el ítem como VERIFICADO. Solo en Pendiente a conferir.
     */
    @Transactional
    public TransferenciaOutput aceptarProducto(Long idTransferencia, Long idDetalle) {
        Transferencia transferencia = buscarPendienteConferirOrThrow(idTransferencia);
        TransferenciaDetalle detalle = encontrarDetalle(transferencia, idDetalle);

        String estadoActual = normalizeDetalleEstado(detalle.getEstado());
        if (DETALLE_VERIFICADO.equals(estadoActual)) {
            return mapper.toOutput(transferencia);
        }

        if (DETALLE_RECHAZADO.equals(estadoActual)) {
            BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            stockService.ajustar(
                    detalle.getProducto().getId_producto(),
                    transferencia.getSectorOrigen().getId_sector(),
                    cantidad.negate()
            );
            stockService.resyncProductoStock(detalle.getProducto().getId_producto());
        }

        detalle.setEstado(DETALLE_VERIFICADO);
        detalle.setMotivoRechazo(null);
        detalle.setMotivoRechazoDetalle(null);
        return mapper.toOutput(actualizar(transferencia));
    }

    /**
     * Marca el ítem como RECHAZADO con motivo. Solo en Pendiente a conferir.
     */
    @Transactional
    public TransferenciaOutput rechazarProducto(
            Long idTransferencia,
            Long idDetalle,
            String motivo,
            String detalleMotivo
    ) {
        Transferencia transferencia = buscarPendienteConferirOrThrow(idTransferencia);
        TransferenciaDetalle detalle = encontrarDetalle(transferencia, idDetalle);

        String motivoNorm = motivo == null ? "" : motivo.trim().toUpperCase(Locale.ROOT);
        if (!MOTIVOS_RECHAZO.contains(motivoNorm)) {
            throw new IllegalArgumentException(
                    "Motivo de rechazo inválido. Use: AVERIADO, VENCIDO, ENVIADO_MAL u OTRO"
            );
        }

        String detalleNorm = detalleMotivo == null ? null : detalleMotivo.trim();
        if (MOTIVO_OTRO.equals(motivoNorm) && (detalleNorm == null || detalleNorm.isEmpty())) {
            throw new IllegalArgumentException("Debe especificar el motivo cuando selecciona OTRO");
        }
        if (detalleNorm != null && detalleNorm.length() > 255) {
            throw new IllegalArgumentException("El detalle del motivo no puede superar 255 caracteres");
        }

        String estadoActual = normalizeDetalleEstado(detalle.getEstado());
        if (!DETALLE_RECHAZADO.equals(estadoActual)) {
            BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            stockService.ajustar(
                    detalle.getProducto().getId_producto(),
                    transferencia.getSectorOrigen().getId_sector(),
                    cantidad
            );
            stockService.resyncProductoStock(detalle.getProducto().getId_producto());
        }

        detalle.setEstado(DETALLE_RECHAZADO);
        detalle.setMotivoRechazo(motivoNorm);
        detalle.setMotivoRechazoDetalle(detalleNorm);
        return mapper.toOutput(actualizar(transferencia));
    }

    /**
     * Avanza la transferencia a la siguiente etapa según el estado actual:
     * CREACION → PENDIENTE_CONFERIR → CONFERIDO → RECEPCIONADO.
     */
    @Transactional
    public TransferenciaOutput avanzarEtapa(Long id) {
        Transferencia transferencia = buscarPorIdOrThrow(id);
        String estado = normalizeCabeceraEstado(transferencia.getEstado());

        return switch (estado) {
            case ESTADO_CREACION, ESTADO_PENDIENTE_LEGADO -> enviarAConferir(transferencia);
            case ESTADO_PENDIENTE_CONFERIR -> conferir(transferencia);
            case ESTADO_CONFERIDO -> recepcionar(transferencia);
            case ESTADO_RECEPCIONADO -> throw new IllegalArgumentException(
                    "La transferencia ya está recepcionada; no hay etapas siguientes"
            );
            default -> throw new IllegalArgumentException(
                    "Estado de transferencia no reconocido: " + transferencia.getEstado()
            );
        };
    }

    /** @deprecated Preferir {@link #avanzarEtapa(Long)}. */
    @Transactional
    public TransferenciaOutput conferir(Long id) {
        Transferencia transferencia = buscarPorIdOrThrow(id);
        String estado = normalizeCabeceraEstado(transferencia.getEstado());
        if (esCreacion(estado)) {
            return enviarAConferir(transferencia);
        }
        if (!ESTADO_PENDIENTE_CONFERIR.equals(estado)) {
            throw new IllegalArgumentException(
                    "Solo se puede conferir desde Pendiente a conferir. Estado actual: "
                            + transferencia.getEstado()
            );
        }
        return conferir(transferencia);
    }

    /** @deprecated Preferir {@link #avanzarEtapa(Long)}. */
    @Transactional
    public TransferenciaOutput recepcionar(Long id) {
        return recepcionar(buscarPorIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransferenciaOutput> findAllPaginated(int page, int size, String filter) {
        return new PageResponse<>(repository.buscar(filter, PageRequest.of(page, size)).map(mapper::toOutput));
    }

    @Transactional(readOnly = true)
    public TransferenciaOutput findById(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    private TransferenciaOutput enviarAConferir(Transferencia transferencia) {
        if (transferencia.getDetalles() == null || transferencia.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe cargar al menos un producto antes de pasar a Pendiente a conferir"
            );
        }
        transferencia.setEstado(ESTADO_PENDIENTE_CONFERIR);
        return mapper.toOutput(actualizar(transferencia));
    }

    private TransferenciaOutput conferir(Transferencia transferencia) {
        if (transferencia.getDetalles() == null || transferencia.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe cargar al menos un producto antes de conferir");
        }

        boolean hayPendientes = transferencia.getDetalles().stream()
                .anyMatch(d -> DETALLE_PENDIENTE.equals(normalizeDetalleEstado(d.getEstado())));
        if (hayPendientes) {
            throw new IllegalArgumentException(
                    "Debe aceptar o rechazar todos los productos antes de conferir"
            );
        }

        boolean hayVerificados = transferencia.getDetalles().stream()
                .anyMatch(d -> DETALLE_VERIFICADO.equals(normalizeDetalleEstado(d.getEstado())));
        if (!hayVerificados) {
            throw new IllegalArgumentException(
                    "Debe haber al menos un producto verificado para conferir"
            );
        }

        transferencia.setEstado(ESTADO_CONFERIDO);
        return mapper.toOutput(actualizar(transferencia));
    }

    private TransferenciaOutput recepcionar(Transferencia transferencia) {
        if (!ESTADO_CONFERIDO.equalsIgnoreCase(normalizeCabeceraEstado(transferencia.getEstado()))) {
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
        int recepcionados = 0;
        for (TransferenciaDetalle detalle : transferencia.getDetalles()) {
            if (!DETALLE_VERIFICADO.equals(normalizeDetalleEstado(detalle.getEstado()))) {
                continue;
            }
            Producto producto = detalle.getProducto();
            BigDecimal cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            stockService.ajustar(producto.getId_producto(), destino.getId_sector(), cantidad);
            productosAfectados.add(producto.getId_producto());
            recepcionados++;
        }
        if (recepcionados == 0) {
            throw new IllegalArgumentException("No hay productos verificados para recepcionar");
        }

        transferencia.setEstado(ESTADO_RECEPCIONADO);
        Transferencia actualizada = actualizar(transferencia);
        for (Long idProducto : productosAfectados) {
            stockService.resyncProductoStock(idProducto);
        }
        return mapper.toOutput(actualizada);
    }

    private Transferencia buscarPendienteConferirOrThrow(Long idTransferencia) {
        Transferencia transferencia = buscarPorIdOrThrow(idTransferencia);
        if (!ESTADO_PENDIENTE_CONFERIR.equals(normalizeCabeceraEstado(transferencia.getEstado()))) {
            throw new IllegalArgumentException(
                    "Solo se pueden revisar productos en la etapa Pendiente a conferir"
            );
        }
        return transferencia;
    }

    private boolean esCreacion(String estado) {
        String n = normalizeCabeceraEstado(estado);
        return ESTADO_CREACION.equals(n) || ESTADO_PENDIENTE_LEGADO.equals(n);
    }

    private TransferenciaDetalle encontrarDetalle(Transferencia transferencia, Long idDetalle) {
        return transferencia.getDetalles().stream()
                .filter(d -> idDetalle.equals(d.getId_detalle()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado con id: " + idDetalle));
    }

    private String normalizeDetalleEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return DETALLE_PENDIENTE;
        }
        return estado.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCabeceraEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return ESTADO_CREACION;
        }
        return estado.trim().toUpperCase(Locale.ROOT);
    }

    private String generarNumero() {
        String prefix = "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long seq = repository.countByNumeroStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

}
