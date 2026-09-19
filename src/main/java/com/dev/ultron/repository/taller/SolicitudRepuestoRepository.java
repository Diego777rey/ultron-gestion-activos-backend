package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.SolicitudRepuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepuestoRepository extends JpaRepository<SolicitudRepuesto, Long> {

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            WHERE s.ordenTrabajo.id_orden_trabajo = :idOrden
            ORDER BY s.fecha DESC
            """)
    List<SolicitudRepuesto> findByOrdenId(@Param("idOrden") Long idOrden);

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            WHERE LOWER(s.ordenTrabajo.numeroOrden) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.estado) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.sectorOrigen.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.sectorDestino.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(s.observacion, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
            """)
    Page<SolicitudRepuesto> search(@Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            LEFT JOIN FETCH s.ordenTrabajo
            LEFT JOIN FETCH s.sectorOrigen
            LEFT JOIN FETCH s.sectorDestino
            ORDER BY s.fecha DESC
            """)
    List<SolicitudRepuesto> findAllParaReporte();

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            LEFT JOIN FETCH s.ordenTrabajo ot
            LEFT JOIN FETCH s.sectorOrigen so
            LEFT JOIN FETCH s.sectorDestino sd
            WHERE LOWER(ot.numeroOrden) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.estado) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(so.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(sd.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY s.fecha DESC
            """)
    List<SolicitudRepuesto> buscarParaReporte(@Param("filter") String filter);

    @Query("""
            SELECT s FROM SolicitudRepuesto s
            LEFT JOIN FETCH s.ordenTrabajo
            LEFT JOIN FETCH s.sectorOrigen
            LEFT JOIN FETCH s.sectorDestino
            WHERE s.id_solicitud_repuesto = :id
            """)
    java.util.Optional<SolicitudRepuesto> findParaReporte(@Param("id") Long id);

    @Query("""
            SELECT s.id_solicitud_repuesto, COUNT(d)
            FROM SolicitudRepuesto s
            LEFT JOIN s.detalles d
            WHERE s.id_solicitud_repuesto IN :ids
            GROUP BY s.id_solicitud_repuesto
            """)
    List<Object[]> contarDetallesParaReporte(@Param("ids") java.util.Collection<Long> ids);
}
