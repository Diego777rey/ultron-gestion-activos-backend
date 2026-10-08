package com.dev.ultron.repository.operaciones;

import com.dev.ultron.domain.operaciones.TransferenciaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaDetalleRepository extends JpaRepository<TransferenciaDetalle, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM TransferenciaDetalle d
            WHERE d.producto.id_producto = :idProducto
            """)
    boolean existsByProducto(@Param("idProducto") Long idProducto);
}
