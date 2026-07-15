package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresentacionProductoRepository extends JpaRepository<PresentacionProducto, Long>, JpaSpecificationExecutor<PresentacionProducto> {

    @Query("SELECT p FROM PresentacionProducto p WHERE p.producto.id_producto = :idProducto ORDER BY p.id_presentacion_producto ASC")
    List<PresentacionProducto> findByProductoOrdenado(@Param("idProducto") Long idProducto);

    @Query("SELECT p FROM PresentacionProducto p WHERE p.producto.id_producto = :idProducto AND p.principal = true")
    List<PresentacionProducto> findPrincipalesDeProducto(@Param("idProducto") Long idProducto);
}
