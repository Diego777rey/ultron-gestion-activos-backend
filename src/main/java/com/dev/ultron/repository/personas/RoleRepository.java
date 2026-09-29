package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("""
            SELECT r FROM Role r
            WHERE r.id IN (
                SELECT ur.role.id FROM UsuarioRole ur
                WHERE ur.usuario.id = :usuarioId
            )
            """)
    Page<Role> findAsignadosByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            SELECT r FROM Role r
            WHERE r.id IN (
                SELECT ur.role.id FROM UsuarioRole ur
                WHERE ur.usuario.id = :usuarioId
            )
            AND UPPER(r.descripcion) LIKE UPPER(CONCAT('%', :search, '%'))
            """)
    Page<Role> findAsignadosByUsuarioIdAndDescripcion(
            @Param("usuarioId") Long usuarioId,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            SELECT r FROM Role r
            WHERE NOT EXISTS (
                SELECT 1 FROM UsuarioRole ur
                WHERE ur.role.id = r.id
                  AND ur.usuario.id = :usuarioId
            )
            """)
    Page<Role> findDisponiblesByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            SELECT r FROM Role r
            WHERE NOT EXISTS (
                SELECT 1 FROM UsuarioRole ur
                WHERE ur.role.id = r.id
                  AND ur.usuario.id = :usuarioId
            )
            AND UPPER(r.descripcion) LIKE UPPER(CONCAT('%', :search, '%'))
            """)
    Page<Role> findDisponiblesByUsuarioIdAndDescripcion(
            @Param("usuarioId") Long usuarioId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT r FROM Role r WHERE LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Role> search(@Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Role r
            LEFT JOIN FETCH r.rolePermisos rp
            LEFT JOIN FETCH rp.permiso
            WHERE r.id IN :ids
            """)
    List<Role> findWithPermisosByIdIn(@Param("ids") Collection<Long> ids);
}
