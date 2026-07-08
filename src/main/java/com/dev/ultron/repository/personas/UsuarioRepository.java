package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.funcionario.id_funcionario = :idFuncionario")
    boolean existsByFuncionarioId(@Param("idFuncionario") Long idFuncionario);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.funcionario.id_funcionario = :idFuncionario AND u.id <> :usuarioId")
    boolean existsByFuncionarioIdAndIdNot(@Param("idFuncionario") Long idFuncionario,
                                          @Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            LEFT JOIN FETCH u.usuarioRoles ur
            LEFT JOIN FETCH ur.role
            LEFT JOIN FETCH u.funcionario f
            LEFT JOIN FETCH f.persona
            """)
    List<Usuario> findAllWithRolesAndFuncionario();

    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.usuarioRoles ur
            LEFT JOIN FETCH ur.role
            LEFT JOIN FETCH u.funcionario f
            LEFT JOIN FETCH f.persona
            WHERE u.id = :id
            """)
    Optional<Usuario> findByIdWithRolesAndFuncionario(Long id);

    @Query(value = "SELECT u FROM Usuario u LEFT JOIN FETCH u.funcionario f LEFT JOIN FETCH f.persona p WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%'))", countQuery = "SELECT COUNT(u) FROM Usuario u LEFT JOIN u.funcionario f LEFT JOIN f.persona p WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%'))")
    org.springframework.data.domain.Page<Usuario> search(@Param("filter") String filter, org.springframework.data.domain.Pageable pageable);
}
