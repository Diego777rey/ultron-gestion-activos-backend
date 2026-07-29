package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.OrdenTrabajoDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenTrabajoDetalleRepository extends JpaRepository<OrdenTrabajoDetalle, Long> {

    @Query("SELECT d FROM OrdenTrabajoDetalle d WHERE d.ordenTrabajo.id_orden_trabajo = :idOrdenTrabajo ORDER BY d.id_detalle ASC")
    List<OrdenTrabajoDetalle> findByOrdenTrabajoId_orden_trabajoOrderById_detalleAsc(@Param("idOrdenTrabajo") Long idOrdenTrabajo);

    @Query("SELECT d FROM OrdenTrabajoDetalle d WHERE d.ordenTrabajo.id_orden_trabajo = :idOrden ORDER BY d.id_detalle ASC")
    Page<OrdenTrabajoDetalle> findByOrdenTrabajoId(@Param("idOrden") Long idOrden, Pageable pageable);
}
