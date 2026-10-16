package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.generic.GenericCrudService;
import com.dev.ultron.generic.SearchNormalizer;
import com.dev.ultron.repository.personas.PersonaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de Persona que extiende el CRUD genérico.
 * Centraliza la lógica de negocio de personas.
 */
@Service
public class PersonaService extends GenericCrudService<Persona, Long> {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    public PersonaService(PersonaRepository personaRepository, PersonaMapper personaMapper) {
        this.personaRepository = personaRepository;
        this.personaMapper = personaMapper;
    }

    @Override
    protected JpaRepository<Persona, Long> getRepository() {
        return personaRepository;
    }

    @Override
    protected void validarAntesDeGuardar(Persona persona) {
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio");
        }
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw new IllegalArgumentException("El documento de la persona es obligatorio");
        }
    }

    /**
     * Busca una persona por CI o RUC, con o sin puntos y sin exigir el dígito verificador.
     */
    @Transactional(readOnly = true)
    public Optional<Persona> buscarPorDocumento(String documento) {
        String base = SearchNormalizer.documentoBase(documento);
        if (base == null) {
            return Optional.empty();
        }
        Optional<Persona> encontrada = personaRepository.findFirstByDocumento(base);
        if (encontrada.isPresent()) {
            return encontrada;
        }
        String compacto = SearchNormalizer.normalize(documento).replaceAll("[\\s.]", "");
        if (!compacto.equals(base)) {
            return personaRepository.findFirstByDocumento(compacto);
        }
        return Optional.empty();
    }

    /**
     * Reutiliza la persona del documento o la crea. Cliente, funcionario,
     * proveedor y vendedor comparten este registro.
     */
    @Transactional
    public Persona reutilizarOCrear(PersonaInput input) {
        String documento = SearchNormalizer.documentoBase(input.documento());
        if (documento == null) {
            throw new IllegalArgumentException("El documento de la persona es obligatorio");
        }
        PersonaInput datos = new PersonaInput(
                input.nombre(),
                input.apellido(),
                documento,
                input.email(),
                input.telefono(),
                input.direccion(),
                input.estado()
        );
        Persona persona = buscarPorDocumento(documento).orElse(null);
        if (persona == null) {
            persona = personaMapper.toEntity(datos);
            if (persona.getEstado() == null || persona.getEstado().isBlank()) {
                persona.setEstado("ACTIVO");
            }
            return guardar(persona);
        }
        personaMapper.updateEntity(persona, datos);
        return actualizar(persona);
    }
}
