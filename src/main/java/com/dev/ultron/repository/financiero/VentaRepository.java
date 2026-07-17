package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
            SELECT v FROM Venta v
            WHERE (:filter IS NULL OR :filter = '' OR LOWER(v.numero) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<Venta> buscar(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.sesionCaja.id_sesion_caja = :idSesion")
    long countBySesion(@Param("idSesion") Long idSesion);
}
