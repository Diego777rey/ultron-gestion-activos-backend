package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenTrabajoDetalleRepository extends JpaRepository<OrdenTrabajoDetalle, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT d FROM OrdenTrabajoDetalle d WHERE d.ordenTrabajo.id_orden_trabajo = :idOrdenTrabajo ORDER BY d.id_detalle ASC")
    List<OrdenTrabajoDetalle> findByOrdenTrabajoId_orden_trabajoOrderById_detalleAsc(@org.springframework.data.repository.query.Param("idOrdenTrabajo") Long idOrdenTrabajo);
}
