package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findFirstByDocumento(String documento);
}
