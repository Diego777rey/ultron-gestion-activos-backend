package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Query("""
        SELECT c FROM Compra c
        LEFT JOIN FETCH c.proveedor
        LEFT JOIN FETCH c.vendedor
        LEFT JOIN FETCH c.sector
        ORDER BY c.fecha_compra DESC
    """)
    List<Compra> findAllWithDetails();

    @Query("""
        SELECT c FROM Compra c
        LEFT JOIN FETCH c.proveedor
        LEFT JOIN FETCH c.vendedor
        LEFT JOIN FETCH c.sector
        LEFT JOIN FETCH c.items
        WHERE c.id_compra = :id
    """)
    Optional<Compra> findByIdWithFull(@Param("id") Long id);

    @Query("""
        SELECT c FROM Compra c
        WHERE c.proveedor.id_proveedor = :idProveedor
        ORDER BY c.fecha_compra DESC
    """)
    List<Compra> findByProveedor(@Param("idProveedor") Long idProveedor);

    @Query("""
        SELECT c FROM Compra c
        WHERE c.estado = :estado
        ORDER BY c.fecha_compra DESC
    """)
    List<Compra> findByEstado(@Param("estado") Compra.EstadoCompra estado);

    @Query("""
        SELECT c FROM Compra c
        WHERE c.fecha_compra BETWEEN :fechaInicio AND :fechaFin
        ORDER BY c.fecha_compra DESC
    """)
    List<Compra> findByFechaRange(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}
