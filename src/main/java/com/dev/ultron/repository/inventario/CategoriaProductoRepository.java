package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.CategoriaProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long>, JpaSpecificationExecutor<CategoriaProducto> {

    @Query("SELECT c FROM CategoriaProducto c WHERE c.categoriaPadre.id_categoria_producto = :idCategoriaPadre ORDER BY c.nombre ASC")
    List<CategoriaProducto> findSubcategorias(@Param("idCategoriaPadre") Long idCategoriaPadre);

    @Query("SELECT c FROM CategoriaProducto c WHERE c.categoriaPadre IS NULL ORDER BY c.nombre ASC")
    List<CategoriaProducto> findRaices();

    @Query("SELECT c FROM CategoriaProducto c WHERE c.categoriaPadre IS NULL")
    Page<CategoriaProducto> findRaicesPaginado(Pageable pageable);
}
