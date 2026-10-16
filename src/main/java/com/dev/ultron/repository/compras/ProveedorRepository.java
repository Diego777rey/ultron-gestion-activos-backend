package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Mismo criterio que el cliente: 80012345, 80012345-0 y el RUC con puntos
     * se consideran el mismo documento.
     */
    @Query("""
            SELECT p FROM Proveedor p
            WHERE UPPER(p.ruc) IN :documentos
                OR UPPER(p.documento) IN :documentos
                OR UPPER(p.ruc) LIKE :prefijoRuc
            ORDER BY p.id_proveedor
            """)
    List<Proveedor> buscarPorRucFlexible(
            @org.springframework.data.repository.query.Param("documentos") Collection<String> documentos,
            @org.springframework.data.repository.query.Param("prefijoRuc") String prefijoRuc);
    
    List<Proveedor> findByActivoTrue();
    
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true ORDER BY " +
           "CASE WHEN p.tipo_persona = 'JURIDICA' THEN p.razon_social " +
           "ELSE CONCAT(p.nombre, ' ', p.apellido) END")
    List<Proveedor> findAllActivosOrdenados();
}
