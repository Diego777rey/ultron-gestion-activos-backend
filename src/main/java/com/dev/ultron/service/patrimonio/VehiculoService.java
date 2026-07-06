package com.dev.ultron.service.patrimonio;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.VehiculoInput;
import com.dev.ultron.dto.patrimonio.mapper.VehiculoMapper;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.repository.patrimonio.VehiculoRepository;
import com.dev.ultron.service.personas.ClienteService;
import com.dev.ultron.utilitarios.StringUtil;

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

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           ClienteService clienteService) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteService = clienteService;
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
        Vehiculo vehiculo = VehiculoMapper.toEntity(input, cliente);
        vehiculo = guardar(vehiculo);
        return VehiculoMapper.toOutput(vehiculo);
    }

    @Transactional
    public VehiculoOutput actualizarVehiculo(Long id, VehiculoInput input) {
        Vehiculo vehiculo = buscarPorIdOrThrow(id);
        Cliente cliente = resolverCliente(input.id_cliente());
        VehiculoMapper.updateEntity(vehiculo, input, cliente);
        vehiculo = actualizar(vehiculo);
        return VehiculoMapper.toOutput(vehiculo);
    }

    @Transactional(readOnly = true)
    public List<VehiculoOutput> listarTodosVehiculos() {
        return listarTodos().stream()
                .map(VehiculoMapper::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoOutput> listarVehiculosPaginado(int page, int size) {
        Page<Vehiculo> pagina = listarPaginado(PageRequest.of(page, size));
        return new PageResponse<>(pagina.map(VehiculoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoOutput> listarVehiculosPorClientePaginado(Long idCliente, int page, int size) {
        clienteService.buscarPorIdOrThrow(idCliente);
        Pageable pageable = PageRequest.of(page, size);
        Page<Vehiculo> pagina = vehiculoRepository.findByClienteId_cliente(idCliente, pageable);
        return new PageResponse<>(pagina.map(VehiculoMapper::toOutput));
    }

    @Transactional(readOnly = true)
    public VehiculoOutput buscarVehiculoPorId(Long id) {
        return VehiculoMapper.toOutput(buscarPorIdOrThrow(id));
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
        if (StringUtil.isNullOrEmpty(chapa)) {
            return;
        }
        if (vehiculoRepository.existsByChapaExcludingId(chapa, idExcluir)) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la chapa: " + chapa);
        }
    }
}
