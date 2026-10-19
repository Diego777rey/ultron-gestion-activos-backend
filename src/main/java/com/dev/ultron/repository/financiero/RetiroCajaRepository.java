package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.RetiroCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RetiroCajaRepository extends JpaRepository<RetiroCaja, Long> {

    @Query("""
            SELECT r FROM RetiroCaja r
            LEFT JOIN FETCH r.responsable u
            LEFT JOIN FETCH u.funcionario f
            LEFT JOIN FETCH f.persona
            WHERE r.sesionCaja.id_sesion_caja = :idSesionCaja
            ORDER BY r.fecha ASC, r.id_retiro_caja ASC
            """)
    List<RetiroCaja> listarPorSesion(@Param("idSesionCaja") Long idSesionCaja);

    @Query("""
            SELECT r FROM RetiroCaja r
            JOIN r.sesionCaja s
            JOIN s.caja c
            LEFT JOIN c.sector sec
            LEFT JOIN r.responsable u
            LEFT JOIN u.funcionario f
            LEFT JOIN f.persona p
            WHERE (:idCaja IS NULL OR c.id_caja = :idCaja)
            AND (:idSector IS NULL OR sec.id_sector = :idSector)
            AND r.fecha >= :fechaDesde
            AND r.fecha <= :fechaHasta
            AND (:filter = ''
                OR LOWER(r.observacion) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(sec.nombre, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(CONCAT(COALESCE(p.nombre, ''), ' ', COALESCE(p.apellido, ''))) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<RetiroCaja> buscarPaginado(
            @Param("idCaja") Long idCaja,
            @Param("idSector") Long idSector,
            @Param("filter") String filter,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable);

    @Query("""
            SELECT r.moneda AS moneda, SUM(r.monto) AS total
            FROM RetiroCaja r
            WHERE r.sesionCaja.id_sesion_caja = :idSesionCaja
            GROUP BY r.moneda
            """)
    List<TotalPorMoneda> totalesPorMoneda(@Param("idSesionCaja") Long idSesionCaja);

    interface TotalPorMoneda {
        String getMoneda();

        BigDecimal getTotal();
    }
}
