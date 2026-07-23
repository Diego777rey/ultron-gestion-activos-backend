package com.dev.ultron.service.taller;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.SolicitudRepuesto;
import com.dev.ultron.domain.taller.SolicitudRepuestoDetalle;
import com.dev.ultron.dto.operaciones.input.TransferenciaDetalleInput;
import com.dev.ultron.dto.operaciones.input.TransferenciaInput;
import com.dev.ultron.dto.operaciones.output.TransferenciaOutput;
import com.dev.ultron.dto.taller.input.SolicitudRepuestoDetalleInput;
import com.dev.ultron.dto.taller.input.SolicitudRepuestoInput;
import com.dev.ultron.dto.taller.mapper.SolicitudRepuestoMapper;
import com.dev.ultron.dto.taller.output.SolicitudRepuestoOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.inventario.ProductoRepository;
import com.dev.ultron.repository.operaciones.TransferenciaRepository;
import com.dev.ultron.repository.sectores.SectorRepository;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import com.dev.ultron.repository.taller.SolicitudRepuestoRepository;
import com.dev.ultron.service.operaciones.TransferenciaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SolicitudRepuestoService extends GenericCrudService<SolicitudRepuesto, Long> {

    private final SolicitudRepuestoRepository repository;
    private final SolicitudRepuestoMapper mapper;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final SectorRepository sectorRepository;
    private final ProductoRepository productoRepository;
    private final TransferenciaService transferenciaService;
    private final TransferenciaRepository transferenciaRepository;

    public SolicitudRepuestoService(
            SolicitudRepuestoRepository repository,
            SolicitudRepuestoMapper mapper,
            OrdenTrabajoRepository ordenTrabajoRepository,
            SectorRepository sectorRepository,
            ProductoRepository productoRepository,
            TransferenciaService transferenciaService,
            TransferenciaRepository transferenciaRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.sectorRepository = sectorRepository;
        this.productoRepository = productoRepository;
        this.transferenciaService = transferenciaService;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Override
    protected JpaRepository<SolicitudRepuesto, Long> getRepository() {
        return repository;
    }

    @Transactional(readOnly = true)
    public List<SolicitudRepuestoOutput> listarPorOrden(Long idOrden) {
        return mapper.toOutputList(repository.findByOrdenId(idOrden));
    }

    @Transactional(readOnly = true)
    public SolicitudRepuestoOutput buscarOutputPorId(Long id) {
        return mapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional
    public SolicitudRepuestoOutput crear(Long idOrden, SolicitudRepuestoInput input) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(idOrden)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada: " + idOrden));

        if (!"EN_PROCESO".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se pueden crear solicitudes de repuesto en etapa EN_PROCESO. Etapa actual: "
                            + orden.getEtapa());
        }
        if (orden.getSector() == null || orden.getSector().getId_sector() == null) {
            throw new IllegalArgumentException(
                    "La orden debe tener un sector asignado (destino de reparaciones)");
        }
        if (input == null || input.id_sector_origen() == null) {
            throw new IllegalArgumentException("Debe indicar el sector origen");
        }
        if (input.detalles() == null || input.detalles().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto");
        }
        if (input.id_sector_origen().equals(orden.getSector().getId_sector())) {
            throw new IllegalArgumentException("El sector origen y destino deben ser distintos");
        }

        Sector origen = sectorRepository.findById(input.id_sector_origen())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sector origen no encontrado: " + input.id_sector_origen()));

        SolicitudRepuesto solicitud = SolicitudRepuesto.builder()
                .ordenTrabajo(orden)
                .sectorOrigen(origen)
                .sectorDestino(orden.getSector())
                .estado("PENDIENTE")
                .observacion(input.observacion() != null ? input.observacion().toUpperCase() : null)
                .build();

        for (SolicitudRepuestoDetalleInput detInput : input.detalles()) {
            if (detInput == null || detInput.id_producto() == null) {
                throw new IllegalArgumentException("Cada detalle debe indicar un producto");
            }
            if (detInput.cantidad() == null || detInput.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }
            Producto producto = productoRepository.findById(detInput.id_producto())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado: " + detInput.id_producto()));
            SolicitudRepuestoDetalle detalle = SolicitudRepuestoDetalle.builder()
                    .solicitudRepuesto(solicitud)
                    .producto(producto)
                    .cantidad(detInput.cantidad())
                    .build();
            solicitud.getDetalles().add(detalle);
        }

        return mapper.toOutput(guardar(solicitud));
    }

    @Transactional
    public SolicitudRepuestoOutput aprobar(Long id) {
        SolicitudRepuesto solicitud = buscarPorIdOrThrow(id);
        if (!"PENDIENTE".equalsIgnoreCase(solicitud.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden aprobar solicitudes PENDIENTES. Estado actual: " + solicitud.getEstado());
        }

        TransferenciaInput transferenciaInput = TransferenciaInput.builder()
                .idSectorOrigen(solicitud.getSectorOrigen().getId_sector())
                .idSectorDestino(solicitud.getSectorDestino().getId_sector())
                .build();

        TransferenciaOutput transferenciaOutput = transferenciaService.registrar(transferenciaInput);
        Long idTransferencia = transferenciaOutput.getId_transferencia();

        for (SolicitudRepuestoDetalle detalle : solicitud.getDetalles()) {
            TransferenciaDetalleInput detInput = TransferenciaDetalleInput.builder()
                    .idProducto(detalle.getProducto().getId_producto())
                    .cantidad(detalle.getCantidad())
                    .build();
            transferenciaService.agregarProducto(idTransferencia, detInput);
        }

        Transferencia transferencia = transferenciaRepository.findById(idTransferencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transferencia no encontrada tras creación: " + idTransferencia));

        solicitud.setTransferencia(transferencia);
        solicitud.setEstado("APROBADA");
        return mapper.toOutput(actualizar(solicitud));
    }

    @Transactional
    public SolicitudRepuestoOutput rechazar(Long id, String motivo) {
        SolicitudRepuesto solicitud = buscarPorIdOrThrow(id);
        if (!"PENDIENTE".equalsIgnoreCase(solicitud.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden rechazar solicitudes PENDIENTES. Estado actual: " + solicitud.getEstado());
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo del rechazo");
        }
        solicitud.setEstado("RECHAZADA");
        solicitud.setMotivoRechazo(motivo.toUpperCase());
        return mapper.toOutput(actualizar(solicitud));
    }
}
