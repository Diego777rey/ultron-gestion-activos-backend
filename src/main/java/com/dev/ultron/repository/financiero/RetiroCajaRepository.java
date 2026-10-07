package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.RetiroCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
