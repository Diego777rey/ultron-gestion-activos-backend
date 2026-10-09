package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM DetalleVenta d
            WHERE d.producto.id_producto = :idProducto
            """)
    boolean existsByProducto(@Param("idProducto") Long idProducto);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM DetalleVenta d
            WHERE d.servicio.id_servicio = :idServicio
            """)
    boolean existsByServicio(@Param("idServicio") Long idServicio);
}
