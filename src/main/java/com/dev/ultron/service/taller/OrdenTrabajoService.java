package com.dev.ultron.service.taller;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.dto.taller.input.OrdenDiagnosticoHallazgoInput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoDetalleInput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;
import com.dev.ultron.dto.taller.mapper.OrdenTrabajoMapper;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import com.dev.ultron.service.taller.orden.OrdenDiagnosticoHallazgoService;
import com.dev.ultron.service.taller.orden.OrdenTrabajoCajaResolver;
import com.dev.ultron.service.taller.orden.OrdenTrabajoDetalleService;
import com.dev.ultron.service.taller.orden.OrdenTrabajoFlujoService;
import com.dev.ultron.service.taller.orden.OrdenTrabajoInputApplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade de Orden de Trabajo: consultas CRUD y delegación a piezas (flujo, detalles, writers).
 */
@Service
public class OrdenTrabajoService extends GenericCrudService<OrdenTrabajo, Long> {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;
    private final OrdenTrabajoInputApplier inputApplier;
    private final OrdenTrabajoFlujoService flujoService;
    private final OrdenTrabajoDetalleService detalleService;
    private final OrdenDiagnosticoHallazgoService hallazgoService;
    private final OrdenTrabajoCajaResolver cajaResolver;

    public OrdenTrabajoService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoMapper ordenTrabajoMapper,
            OrdenTrabajoInputApplier inputApplier,
            OrdenTrabajoFlujoService flujoService,
            OrdenTrabajoDetalleService detalleService,
            OrdenDiagnosticoHallazgoService hallazgoService,
            OrdenTrabajoCajaResolver cajaResolver) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
        this.inputApplier = inputApplier;
        this.flujoService = flujoService;
        this.detalleService = detalleService;
        this.hallazgoService = hallazgoService;
        this.cajaResolver = cajaResolver;
    }

    @Override
    protected JpaRepository<OrdenTrabajo, Long> getRepository() {
        return ordenTrabajoRepository;
    }

    // ==================== CONSULTAS ====================

    @Transactional(readOnly = true)
    public PageResponse<OrdenTrabajoOutput> listarOrdenesPaginado(int page, int size, String filter) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<OrdenTrabajo> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = ordenTrabajoRepository.search(SearchNormalizer.normalizeFilter(filter), pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(ordenTrabajoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public OrdenTrabajoOutput buscarOrdenPorId(Long id) {
        return ordenTrabajoMapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrdenTrabajoOutput> listarOrdenesPorClientePaginado(Long idCliente, int page, int size) {
        Page<OrdenTrabajo> pagina = ordenTrabajoRepository.findByClienteId(idCliente, PageRequest.of(page, size));
        return new PageResponse<>(pagina.map(ordenTrabajoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrdenTrabajoOutput> listarOrdenesPorVehiculoPaginado(Long idVehiculo, int page, int size) {
        Page<OrdenTrabajo> pagina = ordenTrabajoRepository.findByVehiculoId(idVehiculo, PageRequest.of(page, size));
        return new PageResponse<>(pagina.map(ordenTrabajoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public List<OrdenTrabajoOutput> listarAgendaMecanico(Long idMecanico, String fechaDesde, String fechaHasta) {
        LocalDateTime desde = LocalDateTime.parse(fechaDesde.contains("T") ? fechaDesde : fechaDesde + "T00:00:00");
        LocalDateTime hasta = LocalDateTime.parse(fechaHasta.contains("T") ? fechaHasta : fechaHasta + "T23:59:59");
        return ordenTrabajoMapper.toOutputList(
                ordenTrabajoRepository.findAgendaMecanico(idMecanico, desde, hasta));
    }

    @Transactional(readOnly = true)
    public List<CajaOutput> listarCajasConSesionAbierta() {
        return cajaResolver.listarConSesionAbierta();
    }

    // ==================== CRUD ====================

    @Transactional
    public OrdenTrabajoOutput crearOrdenTrabajo(OrdenTrabajoInput input) {
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .numeroOrden(generarNumeroOrden())
                .etapa("RECEPCION")
                .build();
        inputApplier.aplicar(orden, input, true);
        return ordenTrabajoMapper.toOutput(guardar(orden));
    }

    @Transactional
    public OrdenTrabajoOutput actualizarOrdenTrabajo(Long id, OrdenTrabajoInput input) {
        OrdenTrabajo orden = buscarPorIdOrThrow(id);
        inputApplier.aplicar(orden, input, false);
        return ordenTrabajoMapper.toOutput(actualizar(orden));
    }

    @Transactional
    public boolean eliminarOrden(Long id) {
        eliminarPorId(id);
        return true;
    }

    // ==================== DELEGACIÓN A PIEZAS ====================

    public OrdenTrabajoOutput cambiarEtapa(Long id, String nuevaEtapa) {
        return flujoService.cambiarEtapa(id, nuevaEtapa);
    }

    public OrdenTrabajoOutput enviarOrdenACaja(Long idOrden, Long idCaja) {
        return flujoService.enviarACaja(idOrden, idCaja);
    }

    public OrdenTrabajoOutput marcarOrdenFacturada(Long idOrden) {
        return flujoService.marcarFacturada(idOrden);
    }

    public OrdenTrabajoOutput agregarDetalle(Long idOrden, OrdenTrabajoDetalleInput input) {
        return detalleService.agregarDetalle(idOrden, input);
    }

    public OrdenTrabajoOutput eliminarDetalle(Long idOrden, Long idDetalle) {
        return detalleService.eliminarDetalle(idOrden, idDetalle);
    }

    public OrdenTrabajoOutput agregarHallazgo(Long idOrden, OrdenDiagnosticoHallazgoInput input) {
        return hallazgoService.agregarHallazgo(idOrden, input);
    }

    public OrdenTrabajoOutput eliminarHallazgo(Long idOrden, Long idHallazgo) {
        return hallazgoService.eliminarHallazgo(idOrden, idHallazgo);
    }

    private String generarNumeroOrden() {
        Long numero = ordenTrabajoRepository.obtenerSiguienteNumero();
        return String.format("OT-%04d", numero);
    }
}
