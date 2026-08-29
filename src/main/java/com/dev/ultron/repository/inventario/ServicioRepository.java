package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.Servicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {

    @EntityGraph(attributePaths = {"categoriaServicio", "categoriaServicio.categoriaPadre"})
    @Query("SELECT s FROM Servicio s ORDER BY s.nombre ASC")
    List<Servicio> findAllParaReporte();

    @EntityGraph(attributePaths = {"categoriaServicio", "categoriaServicio.categoriaPadre"})
    @Query("""
            SELECT s FROM Servicio s
            WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR (s.codigo IS NOT NULL AND LOWER(s.codigo) LIKE LOWER(CONCAT('%', :filter, '%')))
            ORDER BY s.nombre ASC
            """)
    List<Servicio> buscarParaReporte(@Param("filter") String filter);

    @EntityGraph(attributePaths = {"categoriaServicio", "categoriaServicio.categoriaPadre"})
    @Query("SELECT s FROM Servicio s WHERE s.id_servicio = :id")
    Optional<Servicio> findParaReporte(@Param("id") Long id);
}
