package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @Query("""
            SELECT f FROM Factura f
            WHERE f.numero_factura = :numeroFactura
            AND f.timbrado = :timbrado
            """)
    Optional<Factura> findByNumero_facturaAndTimbrado(
            @Param("numeroFactura") String numeroFactura,
            @Param("timbrado") String timbrado);

    @Query("""
            SELECT f FROM Factura f
            LEFT JOIN FETCH f.cliente c
            LEFT JOIN FETCH c.persona
            LEFT JOIN FETCH f.detalles
            WHERE f.id_factura = :id
            """)
    Optional<Factura> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT f FROM Factura f
            WHERE f.empresa.id_empresa = :idEmpresa
            AND f.estado = :estado
            ORDER BY f.fecha_emision DESC
            """)
    Page<Factura> findByEmpresaAndEstado(
            @Param("idEmpresa") Long idEmpresa,
            @Param("estado") String estado,
            Pageable pageable
    );

    @Query("""
            SELECT f FROM Factura f
            WHERE f.empresa.id_empresa = :idEmpresa
            AND f.fecha_emision BETWEEN :fechaInicio AND :fechaFin
            ORDER BY f.fecha_emision DESC
            """)
    List<Factura> findByEmpresaAndFechaRange(
            @Param("idEmpresa") Long idEmpresa,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("""
            SELECT f FROM Factura f
            WHERE f.cliente.id_cliente = :idCliente
            ORDER BY f.fecha_emision DESC
            """)
    Page<Factura> findByCliente(@Param("idCliente") Long idCliente, Pageable pageable);

    @Query("""
            SELECT f FROM Factura f
            WHERE f.venta.id_venta = :idVenta
            """)
    Optional<Factura> findByVenta(@Param("idVenta") Long idVenta);

    @Query("""
            SELECT f FROM Factura f
            LEFT JOIN FETCH f.empresa
            LEFT JOIN FETCH f.timbradoEntity
            WHERE f.venta.id_venta = :idVenta
            """)
    Optional<Factura> findByVentaConEmisor(@Param("idVenta") Long idVenta);

    @Query("""
            SELECT f FROM Factura f
            WHERE f.empresa.id_empresa = :idEmpresa
            AND (
                LOWER(f.numero_factura) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.cliente_nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.cliente_ruc) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(f.cliente_documento) LIKE LOWER(CONCAT('%', :filter, '%'))
            )
            ORDER BY f.fecha_emision DESC
            """)
    Page<Factura> searchByEmpresa(
            @Param("idEmpresa") Long idEmpresa,
            @Param("filter") String filter,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(f) FROM Factura f
            WHERE f.timbradoEntity.id_timbrado = :idTimbrado
            """)
    long countByTimbrado(@Param("idTimbrado") Long idTimbrado);

    @Query("""
            SELECT f FROM Factura f
            LEFT JOIN FETCH f.cliente c
            LEFT JOIN FETCH c.persona
            LEFT JOIN FETCH f.detalles d
            WHERE f.empresa.id_empresa = :idEmpresa
            AND f.fecha_emision BETWEEN :fechaInicio AND :fechaFin
            ORDER BY f.fecha_emision ASC, f.numero_factura ASC
            """)
    List<Factura> findParaReporteLibroVentas(
            @Param("idEmpresa") Long idEmpresa,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
