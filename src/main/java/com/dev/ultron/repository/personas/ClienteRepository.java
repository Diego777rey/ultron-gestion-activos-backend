package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByPersonaDocumento(String documento);

    Optional<Cliente> findByRuc(String ruc);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE c.persona.documento = :documento")
    boolean existsByPersonaDocumento(@org.springframework.data.repository.query.Param("documento") String documento);
}
