package com.dev.ultron.service.patrimonio;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.VehiculoInput;
import com.dev.ultron.dto.patrimonio.mapper.VehiculoMapper;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.patrimonio.VehiculoRepository;
import com.dev.ultron.service.personas.ClienteService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Vehiculo que extiende el CRUD genérico.
 * Gestiona la lógica de negocio de vehículos patrimoniales,
 * vinculados a un cliente propietario.
 */
@Service
public class VehiculoService extends GenericCrudService<Vehiculo, Long> {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteService clienteService;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           ClienteService clienteService,
                           VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteService = clienteService;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    protected JpaRepository<Vehiculo, Long> getRepository() {
        return vehiculoRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Vehiculo vehiculo) {
        if (vehiculo.getCliente() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio para registrar un vehículo");
        }
        validarChapaUnica(vehiculo.getChapa(), vehiculo.getId_bien());
    }

    @Transactional
    public VehiculoOutput registrarVehiculo(VehiculoInput input) {
        Cliente cliente = resolverCliente(input.id_cliente());
        Vehiculo vehiculo = vehiculoMapper.toEntity(input, cliente);
        vehiculo = guardar(vehiculo);
        return vehiculoMapper.toOutput(vehiculo);
    }

    @Transactional
    public VehiculoOutput actualizarVehiculo(Long id, VehiculoInput input) {
        Vehiculo vehiculo = buscarPorIdOrThrow(id);
        Cliente cliente = resolverCliente(input.id_cliente());
        vehiculoMapper.updateEntity(vehiculo, input, cliente);
        vehiculo = actualizar(vehiculo);
        return vehiculoMapper.toOutput(vehiculo);
    }

    @Transactional(readOnly = true)
    public List<VehiculoOutput> listarTodosVehiculos() {
        return listarTodos().stream()
                .map(vehiculoMapper::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoOutput> listarVehiculosPaginado(int page, int size, String filter) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Vehiculo> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = vehiculoRepository.search(SearchNormalizer.normalizeFilter(filter), pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new PageResponse<>(pagina.map(vehiculoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoOutput> listarVehiculosPorClientePaginado(Long idCliente, int page, int size, String filter) {
        clienteService.buscarPorIdOrThrow(idCliente);
        Pageable pageable = PageRequest.of(page, size);
        Page<Vehiculo> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = vehiculoRepository.searchByClienteId(idCliente, SearchNormalizer.normalizeFilter(filter), pageable);
        } else {
            pagina = vehiculoRepository.findByClienteId(idCliente, pageable);
        }
        return new PageResponse<>(pagina.map(vehiculoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public VehiculoOutput buscarVehiculoPorId(Long id) {
        return vehiculoMapper.toOutput(buscarPorIdOrThrow(id));
    }

    @Transactional
    public boolean eliminarVehiculo(Long id) {
        eliminarPorId(id);
        return true;
    }

    private Cliente resolverCliente(Long idCliente) {
        if (idCliente == null) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        return clienteService.buscarPorIdOrThrow(idCliente);
    }

    private void validarChapaUnica(String chapa, Long idExcluir) {
        String chapaNormalizada = SearchNormalizer.normalize(chapa);
        if (chapaNormalizada == null) {
            return;
        }
        if (vehiculoRepository.existsByChapaExcludingId(chapaNormalizada, idExcluir)) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la chapa: " + chapaNormalizada);
        }
    }
}
