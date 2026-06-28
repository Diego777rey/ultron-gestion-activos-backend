package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByPersonaDocumento(String documento);

    boolean existsByPersonaDocumento(String documento);
}
