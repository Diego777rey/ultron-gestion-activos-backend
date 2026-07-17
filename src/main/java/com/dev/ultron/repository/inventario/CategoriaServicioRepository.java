package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.CategoriaServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Long>, JpaSpecificationExecutor<CategoriaServicio> {

    @Query("SELECT c FROM CategoriaServicio c WHERE c.categoriaPadre.id_categoria_servicio = :idCategoriaPadre ORDER BY c.nombre ASC")
    List<CategoriaServicio> findSubcategorias(@Param("idCategoriaPadre") Long idCategoriaPadre);

    @Query("SELECT c FROM CategoriaServicio c WHERE c.categoriaPadre IS NULL ORDER BY c.nombre ASC")
    List<CategoriaServicio> findRaices();

    @Query("SELECT c FROM CategoriaServicio c WHERE c.categoriaPadre IS NULL")
    Page<CategoriaServicio> findRaicesPaginado(Pageable pageable);
}
