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
}
