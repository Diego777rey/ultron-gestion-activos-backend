package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;

@Repository
public interface PresentacionProductoRepository extends JpaRepository<PresentacionProducto, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "COMMIT"))
    @Query("""
            SELECT p.codigoBarras FROM PresentacionProducto p
            WHERE p.codigoBarras IS NOT NULL
              AND LOWER(p.codigoBarras) IN :codigos
              AND (:idProducto IS NULL OR p.producto.id_producto <> :idProducto)
            """)
    List<String> codigosUsadosEnOtrasPresentaciones(
            @Param("codigos") Collection<String> codigos,
            @Param("idProducto") Long idProducto);

    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "COMMIT"))
    @Query("""
            SELECT COUNT(p) > 0 FROM PresentacionProducto p
            WHERE p.codigoBarras IS NOT NULL
              AND LOWER(p.codigoBarras) = LOWER(:codigo)
              AND (:idProducto IS NULL OR p.producto.id_producto <> :idProducto)
            """)
    boolean codigoUsadoEnOtraPresentacion(@Param("codigo") String codigo, @Param("idProducto") Long idProducto);
}
