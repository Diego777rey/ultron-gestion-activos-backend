package com.dev.ultron.repository.web;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dev.ultron.domain.web.PedidoWeb;

@Repository
public interface PedidoWebRepository extends JpaRepository<PedidoWeb, Long> {

    @Query("""
            SELECT p FROM PedidoWeb p
            LEFT JOIN FETCH p.detalles
            WHERE p.codigo = :codigo
            """)
    Optional<PedidoWeb> findByCodigoConDetalles(@Param("codigo") String codigo);

    boolean existsByCodigo(String codigo);
}
