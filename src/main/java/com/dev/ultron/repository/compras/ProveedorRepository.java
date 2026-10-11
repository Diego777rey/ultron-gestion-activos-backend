package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    
    Optional<Proveedor> findByRuc(String ruc);
    
    List<Proveedor> findByActivoTrue();
    
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true ORDER BY " +
           "CASE WHEN p.tipo_persona = 'JURIDICA' THEN p.razon_social " +
           "ELSE CONCAT(p.nombre, ' ', p.apellido) END")
    List<Proveedor> findAllActivosOrdenados();
}
