package com.dev.ultron.service.taller;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoDetalleInput;
import com.dev.ultron.dto.taller.input.OrdenTrabajoInput;
import com.dev.ultron.dto.taller.mapper.OrdenTrabajoMapper;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import com.dev.ultron.service.patrimonio.VehiculoService;
import com.dev.ultron.service.personas.ClienteService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de Orden de Trabajo que extiende el CRUD genérico.
 * Gestiona el flujo: recepción → diagnóstico → en proceso → finalizada → facturado.
 */
@Service
public class OrdenTrabajoService extends GenericCrudService<OrdenTrabajo, Long> {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;
    private final ClienteService clienteService;
    private final VehiculoService vehiculoService;
    private final SesionCajaRepository sesionCajaRepository;
    private final CajaMapper cajaMapper;

    private final org.springframework.data.jpa.repository.JpaRepository<Funcionario, Long> funcionarioRepo;
    private final org.springframework.data.jpa.repository.JpaRepository<Sector, Long> sectorRepo;
    private final org.springframework.data.jpa.repository.JpaRepository<Usuario, Long> usuarioRepo;
    private final CajaRepository cajaRepo;
    private final org.springframework.data.jpa.repository.JpaRepository<Producto, Long> productoRepo;
    private final org.springframework.data.jpa.repository.JpaRepository<Servicio, Long> servicioRepo;

    public OrdenTrabajoService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoMapper ordenTrabajoMapper,
            ClienteService clienteService,
            VehiculoService vehiculoService,
            SesionCajaRepository sesionCajaRepository,
            CajaMapper cajaMapper,
            com.dev.ultron.repository.personas.FuncionarioRepository funcionarioRepo,
            com.dev.ultron.repository.sectores.SectorRepository sectorRepo,
            com.dev.ultron.repository.personas.UsuarioRepository usuarioRepo,
            CajaRepository cajaRepo,
            com.dev.ultron.repository.inventario.ProductoRepository productoRepo,
            com.dev.ultron.repository.inventario.ServicioRepository servicioRepo) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
        this.clienteService = clienteService;
        this.vehiculoService = vehiculoService;
        this.sesionCajaRepository = sesionCajaRepository;
        this.cajaMapper = cajaMapper;
        this.funcionarioRepo = funcionarioRepo;
        this.sectorRepo = sectorRepo;
        this.usuarioRepo = usuarioRepo;
        this.cajaRepo = cajaRepo;
        this.productoRepo = productoRepo;
        this.servicioRepo = servicioRepo;
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
        return sesionCajaRepository.listarPorEstado("ABIERTA", PageRequest.of(0, 200)).stream()
                .map(SesionCaja::getCaja)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        Caja::getId_caja,
                        c -> c,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .map(cajaMapper::toOutput)
                .toList();
    }

    // ==================== CREAR ORDEN ====================

    @Transactional
    public OrdenTrabajoOutput crearOrdenTrabajo(OrdenTrabajoInput input) {
        OrdenTrabajo orden = OrdenTrabajo.builder()
                .numeroOrden(generarNumeroOrden())
                .etapa("RECEPCION")
                .presupuestoAprobado(false)
                .totalPresupuesto(BigDecimal.ZERO)
                .build();

        aplicarInput(orden, input, true);
        orden = guardar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    // ==================== ACTUALIZAR ORDEN ====================

    @Transactional
    public OrdenTrabajoOutput actualizarOrdenTrabajo(Long id, OrdenTrabajoInput input) {
        OrdenTrabajo orden = buscarPorIdOrThrow(id);
        aplicarInput(orden, input, false);
        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    // ==================== CAMBIAR ETAPA ====================

    @Transactional
    public OrdenTrabajoOutput cambiarEtapa(Long id, String nuevaEtapa) {
        OrdenTrabajo orden = buscarPorIdOrThrow(id);
        String destino = nuevaEtapa.toUpperCase();
        validarTransicionEtapa(orden.getEtapa(), destino);
        validarRequisitosEtapa(orden, destino);

        orden.setEtapa(destino);
        if ("FINALIZADA".equals(destino)) {
            orden.setFechaFinalizacion(LocalDateTime.now());
        }

        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    @Transactional
    public OrdenTrabajoOutput enviarOrdenACaja(Long idOrden, Long idCaja) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        if (!"EN_PROCESO".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se puede enviar a caja una orden en etapa EN_PROCESO. Etapa actual: " + orden.getEtapa());
        }
        Caja caja = exigirCajaConSesionAbierta(idCaja);
        orden.setCaja(caja);
        orden.setEtapa("FINALIZADA");
        orden.setFechaFinalizacion(LocalDateTime.now());
        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    @Transactional
    public OrdenTrabajoOutput marcarOrdenFacturada(Long idOrden) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        if (!"FINALIZADA".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se puede facturar una orden FINALIZADA. Etapa actual: " + orden.getEtapa());
        }
        if (orden.getCaja() == null) {
            throw new IllegalArgumentException("La orden debe tener una caja asignada para facturar");
        }
        orden.setEtapa("FACTURADO");
        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    // ==================== GESTIÓN DE DETALLES ====================

    @Transactional
    public OrdenTrabajoOutput agregarDetalle(Long idOrden, OrdenTrabajoDetalleInput input) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        String etapa = orden.getEtapa();
        if (!"DIAGNOSTICO".equalsIgnoreCase(etapa) && !"EN_PROCESO".equalsIgnoreCase(etapa)) {
            throw new IllegalArgumentException(
                    "Solo se pueden agregar detalles en DIAGNOSTICO o EN_PROCESO. Etapa actual: " + etapa);
        }

        OrdenTrabajoDetalle detalle = OrdenTrabajoDetalle.builder()
                .ordenTrabajo(orden)
                .tipo(input.tipo().toUpperCase())
                .descripcion(input.descripcion() != null ? input.descripcion().toUpperCase() : null)
                .cantidad(input.cantidad() != null ? input.cantidad() : BigDecimal.ONE)
                .precioUnitario(input.precio_unitario() != null ? input.precio_unitario() : BigDecimal.ZERO)
                .etapaOrigen(orden.getEtapa())
                .build();

        if ("PRODUCTO".equalsIgnoreCase(input.tipo())) {
            if (input.id_producto() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio para tipo PRODUCTO");
            }
            Producto producto = productoRepo.findById(input.id_producto())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + input.id_producto()));
            detalle.setProducto(producto);
            if (detalle.getDescripcion() == null) {
                detalle.setDescripcion(producto.getNombre());
            }
            if (input.precio_unitario() == null) {
                detalle.setPrecioUnitario(producto.getPrecioVenta());
            }
        } else if ("SERVICIO".equalsIgnoreCase(input.tipo())) {
            if (input.id_servicio() == null) {
                throw new IllegalArgumentException("El ID del servicio es obligatorio para tipo SERVICIO");
            }
            Servicio servicio = servicioRepo.findById(input.id_servicio())
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + input.id_servicio()));
            detalle.setServicio(servicio);
            if (detalle.getDescripcion() == null) {
                detalle.setDescripcion(servicio.getNombre());
            }
            if (input.precio_unitario() == null) {
                detalle.setPrecioUnitario(servicio.getPrecio());
            }
        } else {
            throw new IllegalArgumentException(
                    "Tipo de detalle inválido: " + input.tipo() + ". Debe ser PRODUCTO o SERVICIO.");
        }

        detalle.setSubtotal(detalle.getCantidad().multiply(detalle.getPrecioUnitario()));
        orden.getDetalles().add(detalle);
        recalcularTotalPresupuesto(orden);
        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    @Transactional
    public OrdenTrabajoOutput eliminarDetalle(Long idOrden, Long idDetalle) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        String etapa = orden.getEtapa();
        if (!"DIAGNOSTICO".equalsIgnoreCase(etapa) && !"EN_PROCESO".equalsIgnoreCase(etapa)) {
            throw new IllegalArgumentException(
                    "Solo se pueden eliminar detalles en DIAGNOSTICO o EN_PROCESO. Etapa actual: " + etapa);
        }
        boolean removed = orden.getDetalles().removeIf(d -> d.getId_detalle().equals(idDetalle));
        if (!removed) {
            throw new EntityNotFoundException("Detalle no encontrado con ID: " + idDetalle);
        }
        recalcularTotalPresupuesto(orden);
        orden = actualizar(orden);
        return ordenTrabajoMapper.toOutput(orden);
    }

    // ==================== ELIMINAR ORDEN ====================

    @Transactional
    public boolean eliminarOrden(Long id) {
        eliminarPorId(id);
        return true;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void aplicarInput(OrdenTrabajo orden, OrdenTrabajoInput input, boolean creando) {
        Cliente cliente = orden.getCliente();
        Vehiculo vehiculo = orden.getVehiculo();

        if (input.id_sector() != null) {
            orden.setSector(sectorRepo.findById(input.id_sector())
                    .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado: " + input.id_sector())));
        }
        if (input.id_responsable() != null) {
            orden.setResponsable(usuarioRepo.findById(input.id_responsable())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Usuario responsable no encontrado: " + input.id_responsable())));
        }
        if (input.id_cliente() != null) {
            cliente = clienteService.buscarPorIdOrThrow(input.id_cliente());
            orden.setCliente(cliente);
        }
        if (input.id_vehiculo() != null) {
            vehiculo = vehiculoService.buscarPorIdOrThrow(input.id_vehiculo());
            orden.setVehiculo(vehiculo);
        }
        if (input.id_mecanico() != null) {
            orden.setMecanico(funcionarioRepo.findById(input.id_mecanico())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado: " + input.id_mecanico())));
        }
        if (input.descripcion_falla() != null) {
            orden.setDescripcionFalla(input.descripcion_falla().toUpperCase());
        }
        if (input.fecha_inicio_estimada() != null && !input.fecha_inicio_estimada().isBlank()) {
            orden.setFechaInicioEstimada(parseDateTime(input.fecha_inicio_estimada()));
        }
        if (input.fecha_fin_estimada() != null && !input.fecha_fin_estimada().isBlank()) {
            orden.setFechaFinEstimada(parseDateTime(input.fecha_fin_estimada()));
        }
        if (input.presupuesto_aprobado() != null) {
            orden.setPresupuestoAprobado(input.presupuesto_aprobado());
        }
        if (input.observaciones() != null) {
            orden.setObservaciones(input.observaciones().toUpperCase());
        }
        if (input.id_caja() != null) {
            orden.setCaja(exigirCajaConSesionAbierta(input.id_caja()));
        }

        if (creando || input.id_cliente() != null || input.id_vehiculo() != null) {
            validarVehiculoPerteneceACliente(orden.getCliente(), orden.getVehiculo());
        }
    }

    private void validarVehiculoPerteneceACliente(Cliente cliente, Vehiculo vehiculo) {
        if (cliente == null || vehiculo == null) {
            return;
        }
        if (vehiculo.getCliente() == null
                || !Objects.equals(vehiculo.getCliente().getId_cliente(), cliente.getId_cliente())) {
            throw new IllegalArgumentException("El vehículo no pertenece al cliente seleccionado");
        }
    }

    private Caja exigirCajaConSesionAbierta(Long idCaja) {
        Caja caja = cajaRepo.findById(idCaja)
                .orElseThrow(() -> new EntityNotFoundException("Caja no encontrada: " + idCaja));
        if (!sesionCajaRepository.existsPorCajaYEstado(idCaja, "ABIERTA")) {
            throw new IllegalArgumentException("La caja no tiene una sesión abierta: " + idCaja);
        }
        return caja;
    }

    private String generarNumeroOrden() {
        Long numero = ordenTrabajoRepository.obtenerSiguienteNumero();
        return String.format("OT-%04d", numero);
    }

    private void recalcularTotalPresupuesto(OrdenTrabajo orden) {
        BigDecimal total = orden.getDetalles().stream()
                .map(OrdenTrabajoDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotalPresupuesto(total);
    }

    private void validarTransicionEtapa(String etapaActual, String nuevaEtapa) {
        boolean valida = switch (etapaActual) {
            case "RECEPCION" -> "DIAGNOSTICO".equalsIgnoreCase(nuevaEtapa);
            case "DIAGNOSTICO" -> "EN_PROCESO".equalsIgnoreCase(nuevaEtapa);
            case "EN_PROCESO" -> "FINALIZADA".equalsIgnoreCase(nuevaEtapa);
            case "FINALIZADA" -> "FACTURADO".equalsIgnoreCase(nuevaEtapa);
            default -> false;
        };
        if (!valida) {
            throw new IllegalArgumentException(
                    "Transición de etapa inválida: " + etapaActual + " → " + nuevaEtapa);
        }
    }

    private void validarRequisitosEtapa(OrdenTrabajo orden, String nuevaEtapa) {
        switch (nuevaEtapa) {
            case "DIAGNOSTICO" -> {
                if (orden.getCliente() == null) {
                    throw new IllegalArgumentException("Debe asignar un cliente antes de pasar a Diagnóstico");
                }
                if (orden.getVehiculo() == null) {
                    throw new IllegalArgumentException("Debe asignar un vehículo antes de pasar a Diagnóstico");
                }
                if (orden.getMecanico() == null) {
                    throw new IllegalArgumentException("Debe asignar un mecánico antes de pasar a Diagnóstico");
                }
                if (orden.getDescripcionFalla() == null || orden.getDescripcionFalla().isBlank()) {
                    throw new IllegalArgumentException("Debe registrar la descripción de la falla");
                }
                validarVehiculoPerteneceACliente(orden.getCliente(), orden.getVehiculo());
            }
            case "EN_PROCESO" -> {
                if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
                    throw new IllegalArgumentException("El presupuesto debe tener al menos un ítem");
                }
                if (!orden.isPresupuestoAprobado()) {
                    throw new IllegalArgumentException("El presupuesto debe estar aprobado por el cliente");
                }
            }
            case "FINALIZADA" -> {
                if (orden.getCaja() == null) {
                    throw new IllegalArgumentException(
                            "Debe asignar una caja abierta antes de finalizar (use enviarOrdenACaja)");
                }
                if (!sesionCajaRepository.existsPorCajaYEstado(orden.getCaja().getId_caja(), "ABIERTA")) {
                    throw new IllegalArgumentException("La caja asignada no tiene sesión abierta");
                }
            }
            case "FACTURADO" -> {
                if (orden.getCaja() == null) {
                    throw new IllegalArgumentException("La orden debe tener una caja asignada para facturar");
                }
            }
            default -> {
            }
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value.contains("T")) {
            return LocalDateTime.parse(value.length() > 19 ? value.substring(0, 19) : value);
        }
        return LocalDateTime.parse(value + "T00:00:00");
    }
}
