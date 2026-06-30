package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.FuncionarioInput;
import com.dev.ultron.dto.personas.mapper.FuncionarioMapper;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.dto.personas.output.FuncionarioOutput;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.repository.personas.FuncionarioRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Funcionario que extiende el CRUD genérico.
 * Gestiona la lógica de negocio de funcionarios, incluyendo la creación
 * de la persona asociada de forma transaccional.
 */
@Service
public class FuncionarioService extends GenericCrudService<Funcionario, Long> {

    private final FuncionarioRepository funcionarioRepository;
    private final PersonaService personaService;
    private final com.dev.ultron.repository.personas.ClienteRepository clienteRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              PersonaService personaService,
                              com.dev.ultron.repository.personas.ClienteRepository clienteRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.personaService = personaService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected JpaRepository<Funcionario, Long> getRepository() {
        return funcionarioRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Funcionario funcionario) {
        if (funcionario.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de persona son obligatorios para el funcionario");
        }
    }

    /**
     * Registra un nuevo funcionario. Reutiliza la persona si ya existe por documento,
     * y crea un registro de cliente automático si no existe.
     */
    @Transactional
    public FuncionarioOutput registrarFuncionario(FuncionarioInput input) {
        String documento = input.persona().documento();
        Persona persona = personaService.buscarPorDocumento(documento).orElse(null);

        if (persona == null) {
            // Crear y guardar la persona
            persona = PersonaMapper.toEntity(input.persona());
            persona = personaService.guardar(persona);
        } else {
            // Actualizar datos de persona existente
            PersonaMapper.updateEntity(persona, input.persona());
            persona = personaService.actualizar(persona);
        }

        // Crear el funcionario con la persona ya persistida
        Funcionario funcionario = FuncionarioMapper.toEntity(input, persona);
        funcionario = guardar(funcionario);

        // Crear cliente automático si no existe
        if (!clienteRepository.existsByPersonaDocumento(persona.getDocumento())) {
            com.dev.ultron.domain.personas.Cliente nuevoCliente = com.dev.ultron.domain.personas.Cliente.builder()
                    .persona(persona)
                    .ruc(persona.getDocumento())
                    .tipoCliente("Persona Física")
                    .fechaRegistro(java.time.LocalDate.now())
                    .estado(true)
                    .build();
            clienteRepository.save(nuevoCliente);
        }

        return FuncionarioMapper.toOutput(funcionario);
    }

    /**
     * Actualiza un funcionario existente y sus datos de persona.
     */
    @Transactional
    public FuncionarioOutput actualizarFuncionario(Long id, FuncionarioInput input) {
        Funcionario funcionario = buscarPorIdOrThrow(id);

        // Actualizar datos de persona
        PersonaMapper.updateEntity(funcionario.getPersona(), input.persona());
        personaService.actualizar(funcionario.getPersona());

        // Actualizar datos de funcionario
        FuncionarioMapper.updateEntity(funcionario, input);
        funcionario = actualizar(funcionario);

        return FuncionarioMapper.toOutput(funcionario);
    }

    /**
     * Lista todos los funcionarios como output DTOs.
     */
    @Transactional(readOnly = true)
    public List<FuncionarioOutput> listarTodosFuncionarios() {
        return listarTodos().stream()
                .map(FuncionarioMapper::toOutput)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.dev.ultron.generic.PageResponse<FuncionarioOutput> listarFuncionariosPaginado(int page, int size) {
        org.springframework.data.domain.Page<Funcionario> pagina = listarPaginado(
            org.springframework.data.domain.PageRequest.of(page, size)
        );
        return new com.dev.ultron.generic.PageResponse<>(
            pagina.map(FuncionarioMapper::toOutput)
        );
    }

    /**
     * Busca un funcionario por ID y retorna como output DTO.
     */
    @Transactional(readOnly = true)
    public FuncionarioOutput buscarFuncionarioPorId(Long id) {
        return FuncionarioMapper.toOutput(buscarPorIdOrThrow(id));
    }

    /**
     * Elimina un funcionario por su ID.
     * No elimina la persona asociada para mantener integridad referencial.
     */
    @Transactional
    public boolean eliminarFuncionario(Long id) {
        eliminarPorId(id);
        return true;
    }
}
