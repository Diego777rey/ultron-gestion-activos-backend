package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.ClienteInput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.personas.ClienteRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Cliente que extiende el CRUD genérico.
 * Gestiona la lógica de negocio de clientes, incluyendo la creación
 * de la persona asociada de forma transaccional.
 */
@Service
public class ClienteService extends GenericCrudService<Cliente, Long> {

    private final ClienteRepository clienteRepository;
    private final PersonaService personaService;
    private final PersonaMapper personaMapper;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository,
                          PersonaService personaService,
                          PersonaMapper personaMapper,
                          ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.personaService = personaService;
        this.personaMapper = personaMapper;
        this.clienteMapper = clienteMapper;
    }

    @Override
    protected JpaRepository<Cliente, Long> getRepository() {
        return clienteRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Cliente cliente) {
        if (cliente.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de persona son obligatorios para el cliente");
        }
    }

    /**
     * Registra un nuevo cliente. Reutiliza la persona si ya existe por documento.
     */
    @Transactional
    public ClienteOutput registrarCliente(ClienteInput input) {
        String documento = input.persona().documento();
        Persona persona = personaService.buscarPorDocumento(documento).orElse(null);

        if (persona == null) {
            // Crear y guardar la persona
            persona = personaMapper.toEntity(input.persona());
            persona = personaService.guardar(persona);
        } else {
            // Actualizar datos de persona existente
            personaMapper.updateEntity(persona, input.persona());
            persona = personaService.actualizar(persona);
        }

        // Crear el cliente con la persona ya persistida
        Cliente cliente = clienteMapper.toEntity(input, persona);
        cliente = guardar(cliente);

        return clienteMapper.toOutput(cliente);
    }

    /**
     * Actualiza un cliente existente y sus datos de persona.
     */
    @Transactional
    public ClienteOutput actualizarCliente(Long id, ClienteInput input) {
        Cliente cliente = buscarPorIdOrThrow(id);

        // Actualizar datos de persona
        personaMapper.updateEntity(cliente.getPersona(), input.persona());
        personaService.actualizar(cliente.getPersona());

        // Actualizar datos de cliente
        clienteMapper.updateEntity(cliente, input);
        cliente = actualizar(cliente);

        return clienteMapper.toOutput(cliente);
    }

    /**
     * Lista todos los clientes como output DTOs.
     */
    @Transactional(readOnly = true)
    public List<ClienteOutput> listarTodosClientes() {
        return listarTodos().stream()
                .map(clienteMapper::toOutput)
                .toList();
    }

    /**
     * Lista clientes de forma paginada y retorna un PageResponse DTO.
     */
    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<ClienteOutput> listarClientesPaginado(int page, int size, String filter) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Cliente> pagina;
        if (filter != null && !filter.trim().isEmpty()) {
            pagina = clienteRepository.search(com.dev.ultron.generic.SearchNormalizer.normalizeFilter(filter), pageRequest);
        } else {
            pagina = listarPaginado(pageRequest);
        }
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(clienteMapper::toOutput)
        );
    }

    /**
     * Busca un cliente por ID y retorna como output DTO.
     */
    @Transactional(readOnly = true)
    public ClienteOutput buscarClientePorId(Long id) {
        return clienteMapper.toOutput(buscarPorIdOrThrow(id));
    }

    /**
     * Crea un cliente automático si la persona aún no tiene uno asociado.
     * Usado al registrar funcionarios para mantener consistencia de datos.
     */
    @Transactional
    public void crearClienteAutomaticoSiNoExiste(Persona persona) {
        if (clienteRepository.existsByPersonaDocumento(persona.getDocumento())) {
            return;
        }
        guardar(clienteMapper.toAutomaticoFromPersona(persona));
    }

    /**
     * Elimina un cliente por su ID.
     * No elimina la persona asociada para mantener integridad referencial.
     */
    @Transactional
    public boolean eliminarCliente(Long id) {
        eliminarPorId(id);
        return true;
    }
}
