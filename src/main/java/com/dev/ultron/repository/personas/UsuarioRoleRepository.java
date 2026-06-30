package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.UsuarioRole;
import com.dev.ultron.domain.personas.UsuarioRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRoleRepository extends JpaRepository<UsuarioRole, UsuarioRoleId> {

    void deleteByUsuario_Id(Long usuarioId);
}
