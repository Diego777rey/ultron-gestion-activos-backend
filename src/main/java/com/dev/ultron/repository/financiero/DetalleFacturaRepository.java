package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {

    @Query("""
            SELECT d FROM DetalleFactura d
            WHERE d.factura.id_factura = :idFactura
            ORDER BY d.numero_linea ASC
            """)
    List<DetalleFactura> findByFactura(@Param("idFactura") Long idFactura);

    @Query("""
            SELECT d FROM DetalleFactura d
            LEFT JOIN FETCH d.producto
            LEFT JOIN FETCH d.servicio
            WHERE d.factura.id_factura = :idFactura
            ORDER BY d.numero_linea ASC
            """)
    List<DetalleFactura> findByFacturaWithDetails(@Param("idFactura") Long idFactura);
}
