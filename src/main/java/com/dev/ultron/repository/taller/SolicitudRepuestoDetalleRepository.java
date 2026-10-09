package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.SolicitudRepuestoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepuestoDetalleRepository extends JpaRepository<SolicitudRepuestoDetalle, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM SolicitudRepuestoDetalle d
            WHERE d.producto.id_producto = :idProducto
            """)
    boolean existsByProducto(@Param("idProducto") Long idProducto);
}
