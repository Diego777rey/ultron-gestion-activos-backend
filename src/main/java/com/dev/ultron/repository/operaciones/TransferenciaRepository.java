package com.dev.ultron.repository.operaciones;

import com.dev.ultron.domain.operaciones.Transferencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    @Query("""
            SELECT t FROM Transferencia t
            LEFT JOIN t.sectorOrigen so
            LEFT JOIN t.sectorDestino sd
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(t.numero) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(so.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(sd.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))
            ORDER BY t.fecha DESC
            """)
    Page<Transferencia> buscar(@Param("filter") String filter, Pageable pageable);

    long countByNumeroStartingWith(String prefix);
}
