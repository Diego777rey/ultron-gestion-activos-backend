package com.dev.ultron.repository.compras;

import com.dev.ultron.domain.compras.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    List<Vendedor> findByActivoTrue();
    
    @Query("SELECT v FROM Vendedor v WHERE v.proveedor.id_proveedor = :idProveedor AND v.activo = true " +
           "ORDER BY v.nombre, v.apellido")
    List<Vendedor> findByProveedorActivosOrdenados(@Param("idProveedor") Long idProveedor);
}
