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

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cliente c WHERE LOWER(c.persona.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(c.persona.apellido) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(c.persona.documento) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(c.ruc) LIKE LOWER(CONCAT('%', :filter, '%'))")
    org.springframework.data.domain.Page<Cliente> search(@org.springframework.data.repository.query.Param("filter") String filter, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cliente c ORDER BY c.persona.nombre, c.persona.apellido")
    java.util.List<Cliente> findAllParaReporte();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.persona.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(c.persona.apellido) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(c.persona.documento) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(c.ruc) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY c.persona.nombre, c.persona.apellido
            """)
    java.util.List<Cliente> buscarParaReporte(@org.springframework.data.repository.query.Param("filter") String filter);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cliente c WHERE c.id_cliente = :id")
    java.util.Optional<Cliente> findParaReporte(@org.springframework.data.repository.query.Param("id") Long id);
}
