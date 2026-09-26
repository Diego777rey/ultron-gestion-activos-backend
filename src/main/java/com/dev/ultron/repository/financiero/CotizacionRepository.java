package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Cotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    @Query("""
            SELECT c FROM Cotizacion c
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(c.moneda) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR CONCAT(c.id_cotizacion, '') LIKE CONCAT('%', :filter, '%'))
            ORDER BY c.moneda ASC
            """)
    Page<Cotizacion> buscar(@Param("filter") String filter, Pageable pageable);
    
    @Query("""
            SELECT c FROM Cotizacion c
            WHERE c.activa = true
            ORDER BY c.moneda ASC
            """)
    List<Cotizacion> findAllActivas();
    
    @Query("""
            SELECT c FROM Cotizacion c
            WHERE UPPER(c.moneda) = UPPER(:moneda)
                AND c.activa = true
            ORDER BY c.fechaActualizacion DESC
            LIMIT 1
            """)
    Cotizacion findActivaByMoneda(@Param("moneda") String moneda);
}
