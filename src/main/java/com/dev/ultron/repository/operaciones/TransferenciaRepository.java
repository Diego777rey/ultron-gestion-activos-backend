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

    @Query("""
            SELECT t FROM Transferencia t
            LEFT JOIN FETCH t.sectorOrigen
            LEFT JOIN FETCH t.sectorDestino
            ORDER BY t.fecha DESC
            """)
    java.util.List<Transferencia> findAllParaReporte();

    @Query("""
            SELECT t FROM Transferencia t
            LEFT JOIN FETCH t.sectorOrigen so
            LEFT JOIN FETCH t.sectorDestino sd
            WHERE LOWER(t.numero) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(so.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(sd.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY t.fecha DESC
            """)
    java.util.List<Transferencia> buscarParaReporte(@Param("filter") String filter);

    @Query("""
            SELECT t FROM Transferencia t
            LEFT JOIN FETCH t.sectorOrigen
            LEFT JOIN FETCH t.sectorDestino
            WHERE t.id_transferencia = :id
            """)
    java.util.Optional<Transferencia> findParaReporte(@Param("id") Long id);

    @Query("""
            SELECT t.id_transferencia, COUNT(d)
            FROM Transferencia t
            LEFT JOIN t.detalles d
            WHERE t.id_transferencia IN :ids
            GROUP BY t.id_transferencia
            """)
    java.util.List<Object[]> contarDetallesParaReporte(@Param("ids") java.util.Collection<Long> ids);
}
