package com.dev.ultron.service.personas;

import com.dev.ultron.domain.personas.Persona;
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

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
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
        if (persona.getApellido() == null || persona.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido de la persona es obligatorio");
        }
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw new IllegalArgumentException("El documento de la persona es obligatorio");
        }
    }

    /**
     * Busca una persona por su documento.
     */
    @Transactional(readOnly = true)
    public Optional<Persona> buscarPorDocumento(String documento) {
        String documentoNormalizado = SearchNormalizer.normalize(documento);
        if (documentoNormalizado == null) {
            return Optional.empty();
        }
        return personaRepository.findFirstByDocumento(documentoNormalizado);
    }
}
