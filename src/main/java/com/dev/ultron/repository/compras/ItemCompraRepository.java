package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.ItemCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {

    @Query("""
        SELECT ic FROM ItemCompra ic
        WHERE ic.compra.id_compra = :idCompra
        ORDER BY ic.id_item_compra ASC
    """)
    List<ItemCompra> findByCompra(@Param("idCompra") Long idCompra);

    @Query("""
        SELECT ic FROM ItemCompra ic
        WHERE ic.producto.id_producto = :idProducto
        ORDER BY ic.fecha_registro DESC
    """)
    List<ItemCompra> findByProducto(@Param("idProducto") Long idProducto);

    @Query("""
        SELECT ic FROM ItemCompra ic
        JOIN FETCH ic.compra c
        WHERE ic.producto.id_producto = :idProducto
        AND c.proveedor.id_proveedor = :idProveedor
        ORDER BY c.fecha_compra DESC
    """)
    List<ItemCompra> findHistorialCompraProductoProveedor(
        @Param("idProducto") Long idProducto,
        @Param("idProveedor") Long idProveedor
    );
}
