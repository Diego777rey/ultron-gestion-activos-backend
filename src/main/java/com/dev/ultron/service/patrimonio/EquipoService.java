package com.dev.ultron.service.patrimonio;

import com.dev.ultron.domain.patrimonio.Equipo;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.EquipoInput;
import com.dev.ultron.dto.patrimonio.mapper.EquipoMapper;
import com.dev.ultron.dto.patrimonio.output.EquipoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.patrimonio.EquipoRepository;
import com.dev.ultron.service.personas.ClienteService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Equipos de clientes (ECU, tablero, BCM...). El vehículo es opcional,
 * pero si se indica debe pertenecer al mismo cliente.
 */
@Service
public class EquipoService extends GenericCrudService<Equipo, Long> {

    private final EquipoRepository equipoRepository;
    private final ClienteService clienteService;
    private final VehiculoService vehiculoService;
    private final EquipoMapper equipoMapper;

    public EquipoService(EquipoRepository equipoRepository,
                         ClienteService clienteService,
                         VehiculoService vehiculoService,
                         EquipoMapper equipoMapper) {
        this.equipoRepository = equipoRepository;
        this.clienteService = clienteService;
        this.vehiculoService = vehiculoService;
        this.equipoMapper = equipoMapper;
    }

    @Override
    protected JpaRepository<Equipo, Long> getRepository() {
        return equipoRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Equipo equipo) {
        if (equipo.getCliente() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio para registrar un equipo");
        }
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de equipo es obligatorio");
        }
        validarVehiculoDelCliente(equipo.getCliente(), equipo.getVehiculo());
    }

    @Transactional
    public EquipoOutput registrarEquipo(EquipoInput input) {
        Cliente cliente = resolverCliente(input.id_cliente());
        Equipo equipo = equipoMapper.toEntity(input, cliente);
        equipo.setVehiculo(resolverVehiculo(input.id_vehiculo()));
        return equipoMapper.toOutput(guardar(equipo));
    }

    @Transactional
    public EquipoOutput actualizarEquipo(Long id, EquipoInput input) {
        Equipo equipo = buscarPorIdOrThrow(id);
        Cliente cliente = resolverCliente(input.id_cliente());
        equipoMapper.updateEntity(equipo, input, cliente);
        equipo.setVehiculo(resolverVehiculo(input.id_vehiculo()));
        return equipoMapper.toOutput(actualizar(equipo));
    }

    @Transactional(readOnly = true)
    public List<EquipoOutput> listarTodosEquipos() {
        return listarTodos().stream().map(equipoMapper::toOutput).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipoOutput> listarEquiposPaginado(int page, int size, String filter) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Equipo> pagina = filter != null && !filter.trim().isEmpty()
                ? equipoRepository.search(SearchNormalizer.normalizeFilter(filter), pageRequest)
                : equipoRepository.findAllOrdenados(pageRequest);
        return new PageResponse<>(pagina.map(equipoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipoOutput> listarEquiposPorClientePaginado(Long idCliente, int page, int size, String filter) {
        clienteService.buscarPorIdOrThrow(idCliente);
        Pageable pageable = PageRequest.of(page, size);
        Page<Equipo> pagina = filter != null && !filter.trim().isEmpty()
                ? equipoRepository.searchByClienteId(idCliente, SearchNormalizer.normalizeFilter(filter), pageable)
                : equipoRepository.findByClienteId(idCliente, pageable);
        return new PageResponse<>(pagina.map(equipoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public EquipoOutput buscarEquipoPorId(Long id) {
        return equipoMapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional
    public boolean eliminarEquipo(Long id) {
        eliminarPorId(id);
        return true;
    }

    public void validarVehiculoDelCliente(Cliente cliente, Vehiculo vehiculo) {
        if (cliente == null || vehiculo == null) {
            return;
        }
        if (vehiculo.getCliente() == null
                || !Objects.equals(vehiculo.getCliente().getId_cliente(), cliente.getId_cliente())) {
            throw new IllegalArgumentException("El vehículo del equipo no pertenece al cliente seleccionado");
        }
    }

    private Cliente resolverCliente(Long idCliente) {
        if (idCliente == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        return clienteService.buscarPorIdOrThrow(idCliente);
    }

    private Vehiculo resolverVehiculo(Long idVehiculo) {
        return idVehiculo == null ? null : vehiculoService.buscarPorIdOrThrow(idVehiculo);
    }
}
