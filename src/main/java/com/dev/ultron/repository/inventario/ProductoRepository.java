package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
                    AND LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%'))))
            ORDER BY p.nombre ASC
            """)
    Page<Producto> buscar(@Param("filter") String filter, Pageable pageable);

    @EntityGraph(attributePaths = {"categoriaProducto", "categoriaProducto.categoriaPadre"})
    @Query("SELECT p FROM Producto p ORDER BY p.nombre ASC")
    List<Producto> findAllParaReporte();

    @EntityGraph(attributePaths = {"categoriaProducto", "categoriaProducto.categoriaPadre"})
    @Query("""
            SELECT p FROM Producto p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR (p.codigoBarras IS NOT NULL
                    AND LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :filter, '%')))
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscarParaReporte(@Param("filter") String filter);

    @EntityGraph(attributePaths = {"categoriaProducto", "categoriaProducto.categoriaPadre"})
    @Query("SELECT p FROM Producto p WHERE p.id_producto = :id")
    Optional<Producto> findParaReporte(@Param("id") Long id);
}
