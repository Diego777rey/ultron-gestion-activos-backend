package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
            SELECT v FROM Venta v
            WHERE (:filter IS NULL OR :filter = ''
                OR CONCAT(v.id_venta, '') LIKE CONCAT('%', :filter, '%'))
            """)
    Page<Venta> buscar(@Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT v FROM Venta v
            WHERE v.sesionCaja.id_sesion_caja = :idSesionCaja
            AND (:filter IS NULL OR :filter = ''
                OR CONCAT(v.id_venta, '') LIKE CONCAT('%', :filter, '%'))
            """)
    Page<Venta> buscarPorSesion(
            @Param("idSesionCaja") Long idSesionCaja,
            @Param("filter") String filter,
            Pageable pageable);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.sesionCaja.id_sesion_caja = :idSesion")
    long countBySesion(@Param("idSesion") Long idSesion);

    @Query("""
            SELECT v FROM Venta v
            WHERE v.sesionCaja.id_sesion_caja = :idSesionCaja
            ORDER BY v.fecha DESC
            """)
    List<Venta> findBySesionCaja_IdSesionCajaOrderByFechaDesc(@Param("idSesionCaja") Long idSesionCaja);

    /** Cantidad y total en guaraníes de las ventas no anuladas de la sesión, por forma de pago. */
    @Query("""
            SELECT COALESCE(v.formaPago, 'EFECTIVO') AS formaPago,
                   COUNT(v) AS cantidad,
                   COALESCE(SUM(v.total), 0) AS total
            FROM Venta v
            WHERE v.sesionCaja.id_sesion_caja = :idSesionCaja
            AND (v.estado IS NULL OR v.estado <> 'ANULADA')
            GROUP BY COALESCE(v.formaPago, 'EFECTIVO')
            """)
    List<TotalVentasPorFormaPago> totalesPorFormaPago(@Param("idSesionCaja") Long idSesionCaja);

    /** Ventas cobradas en efectivo (no anuladas) con lo que entró y salió del cajón. */
    @Query("""
            SELECT v.moneda AS moneda,
                   v.total AS total,
                   v.montoMonedaOriginal AS montoMonedaOriginal,
                   v.montoRecibido AS montoRecibido,
                   v.monedaVuelto AS monedaVuelto,
                   v.vuelto AS vuelto
            FROM Venta v
            WHERE v.sesionCaja.id_sesion_caja = :idSesionCaja
            AND COALESCE(v.formaPago, 'EFECTIVO') = 'EFECTIVO'
            AND (v.estado IS NULL OR v.estado <> 'ANULADA')
            """)
    List<CobroEfectivo> cobrosEnEfectivo(@Param("idSesionCaja") Long idSesionCaja);

    interface CobroEfectivo {
        /** Moneda de cobro con el nombre de la cotización (PYG, DOLAR, REAL...). */
        String getMoneda();

        /** En guaraníes. */
        BigDecimal getTotal();

        BigDecimal getMontoMonedaOriginal();

        /** Lo que entregó el cliente en la moneda de cobro; null si no se informó. */
        BigDecimal getMontoRecibido();

        String getMonedaVuelto();

        BigDecimal getVuelto();
    }

    interface TotalVentasPorFormaPago {
        String getFormaPago();

        Long getCantidad();

        BigDecimal getTotal();
    }
}
