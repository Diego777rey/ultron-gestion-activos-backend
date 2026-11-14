package com.dev.ultron.repository.web;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dev.ultron.domain.web.CuentaClienteWeb;

@Repository
public interface CuentaClienteWebRepository extends JpaRepository<CuentaClienteWeb, Long> {

    @Query("""
            SELECT c FROM CuentaClienteWeb c
            JOIN FETCH c.cliente cl
            JOIN FETCH cl.persona
            WHERE LOWER(c.email) = LOWER(:email)
            """)
    Optional<CuentaClienteWeb> findByEmailIgnoreCase(@Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM CuentaClienteWeb c
            WHERE LOWER(c.email) = LOWER(:email)
            """)
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("""
            SELECT c FROM CuentaClienteWeb c
            JOIN FETCH c.cliente cl
            JOIN FETCH cl.persona
            WHERE cl.id_cliente = :idCliente
            """)
    Optional<CuentaClienteWeb> findByClienteId(@Param("idCliente") Long idCliente);
}
