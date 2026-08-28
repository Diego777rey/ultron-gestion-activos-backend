package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByPersonaDocumento(String documento);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Funcionario f WHERE f.persona.documento = :documento")
    boolean existsByPersonaDocumento(@org.springframework.data.repository.query.Param("documento") String documento);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Funcionario f WHERE LOWER(f.persona.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(f.persona.apellido) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(f.persona.documento) LIKE LOWER(CONCAT('%', :filter, '%'))")
    org.springframework.data.domain.Page<Funcionario> search(@org.springframework.data.repository.query.Param("filter") String filter, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("SELECT f FROM Funcionario f ORDER BY f.persona.nombre, f.persona.apellido")
    java.util.List<Funcionario> findAllParaReporte();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("""
            SELECT f FROM Funcionario f
            WHERE LOWER(f.persona.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.persona.apellido) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.persona.documento) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.sector) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY f.persona.nombre, f.persona.apellido
            """)
    java.util.List<Funcionario> buscarParaReporte(@org.springframework.data.repository.query.Param("filter") String filter);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"persona"})
    @org.springframework.data.jpa.repository.Query("SELECT f FROM Funcionario f WHERE f.id_funcionario = :id")
    java.util.Optional<Funcionario> findParaReporte(@org.springframework.data.repository.query.Param("id") Long id);
}
