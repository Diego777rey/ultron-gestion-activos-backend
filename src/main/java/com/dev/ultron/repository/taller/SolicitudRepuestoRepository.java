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
}
