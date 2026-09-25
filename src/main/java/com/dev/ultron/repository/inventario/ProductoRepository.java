package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    @Query("""
            SELECT p FROM Producto p
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR (p.codigoBarras IS NOT NULL
                    AND LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%')))
                OR EXISTS (
                    SELECT 1 FROM PresentacionProducto pp
                    WHERE pp.producto = p
                      AND pp.codigoBarras IS NOT NULL
                      AND LOWER(pp.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%'))))
            ORDER BY p.nombre ASC
            """)
    Page<Producto> buscar(@Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.categoriaProducto cp
            LEFT JOIN FETCH cp.categoriaPadre
            ORDER BY p.nombre ASC
            """)
    List<Producto> findAllParaReporte();

    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.categoriaProducto cp
            LEFT JOIN FETCH cp.categoriaPadre
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR (p.codigoBarras IS NOT NULL
                    AND LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%')))
                OR EXISTS (
                    SELECT 1 FROM PresentacionProducto pp
                    WHERE pp.producto = p
                      AND pp.codigoBarras IS NOT NULL
                      AND LOWER(pp.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%')))
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscarParaReporte(@Param("filter") String filter);

    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "COMMIT"))
    @Query("""
            SELECT p.codigoBarras FROM Producto p
            WHERE p.codigoBarras IS NOT NULL
              AND LOWER(p.codigoBarras) IN :codigos
              AND (:idProducto IS NULL OR p.id_producto <> :idProducto)
            """)
    List<String> codigosBarrasUsadosPorOtrosProductos(
            @Param("codigos") Collection<String> codigos,
            @Param("idProducto") Long idProducto);

    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.categoriaProducto cp
            LEFT JOIN FETCH cp.categoriaPadre
            WHERE p.id_producto = :id
            """)
    Optional<Producto> findParaReporte(@Param("id") Long id);
}
