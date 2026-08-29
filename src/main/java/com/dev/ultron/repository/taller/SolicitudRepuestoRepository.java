package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.SolicitudRepuesto;
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
            SELECT DISTINCT s FROM SolicitudRepuesto s
            LEFT JOIN FETCH s.ordenTrabajo
            LEFT JOIN FETCH s.sectorOrigen
            LEFT JOIN FETCH s.sectorDestino
            LEFT JOIN FETCH s.detalles
            ORDER BY s.fecha DESC
            """)
    List<SolicitudRepuesto> findAllParaReporte();

    @Query("""
            SELECT DISTINCT s FROM SolicitudRepuesto s
            LEFT JOIN FETCH s.ordenTrabajo ot
            LEFT JOIN FETCH s.sectorOrigen so
            LEFT JOIN FETCH s.sectorDestino sd
            LEFT JOIN FETCH s.detalles
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
            LEFT JOIN FETCH s.detalles
            WHERE s.id_solicitud_repuesto = :id
            """)
    java.util.Optional<SolicitudRepuesto> findParaReporte(@Param("id") Long id);
}
