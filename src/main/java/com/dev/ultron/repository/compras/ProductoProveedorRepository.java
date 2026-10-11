package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {

    @Query("""
        SELECT pp FROM ProductoProveedor pp
        WHERE pp.producto.id_producto = :idProducto
        AND pp.activo = true
        ORDER BY pp.fecha_ultima_compra DESC
    """)
    List<ProductoProveedor> findByProductoActivos(@Param("idProducto") Long idProducto);

    @Query("""
        SELECT pp FROM ProductoProveedor pp
        WHERE pp.proveedor.id_proveedor = :idProveedor
        AND pp.activo = true
        ORDER BY pp.producto.nombre ASC
    """)
    List<ProductoProveedor> findByProveedorActivos(@Param("idProveedor") Long idProveedor);

    @Query("""
        SELECT pp FROM ProductoProveedor pp
        WHERE pp.producto.id_producto = :idProducto
        AND pp.proveedor.id_proveedor = :idProveedor
    """)
    Optional<ProductoProveedor> findByProductoAndProveedor(
        @Param("idProducto") Long idProducto,
        @Param("idProveedor") Long idProveedor
    );
}
