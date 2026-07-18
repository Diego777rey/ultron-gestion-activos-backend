package com.dev.ultron.repository.operaciones;

import com.dev.ultron.domain.operaciones.StockProductoSector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface StockProductoSectorRepository extends JpaRepository<StockProductoSector, Long> {

    @Query("""
            SELECT s FROM StockProductoSector s
            WHERE s.producto.id_producto = :idProducto
              AND s.sector.id_sector = :idSector
            """)
    Optional<StockProductoSector> findByProductoAndSector(
            @Param("idProducto") Long idProducto,
            @Param("idSector") Long idSector
    );

    @Query("""
            SELECT COALESCE(SUM(s.cantidad), 0) FROM StockProductoSector s
            WHERE s.producto.id_producto = :idProducto
            """)
    BigDecimal sumCantidadByProducto(@Param("idProducto") Long idProducto);

}
