package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.id IN (SELECT ur.role.id FROM UsuarioRole ur WHERE ur.usuario.id = :usuarioId) AND (:search IS NULL OR UPPER(r.descripcion) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<Role> findRolesByUsuarioIdPaginado(@Param("usuarioId") Long usuarioId, @Param("search") String search, Pageable pageable);

    @Query("SELECT r FROM Role r WHERE r.id NOT IN (SELECT ur.role.id FROM UsuarioRole ur WHERE ur.usuario.id = :usuarioId) AND (:search IS NULL OR UPPER(r.descripcion) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<Role> findRolesDisponiblesByUsuarioIdPaginado(@Param("usuarioId") Long usuarioId, @Param("search") String search, Pageable pageable);

    @Query("SELECT r FROM Role r WHERE LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Role> search(@Param("filter") String filter, Pageable pageable);
}
