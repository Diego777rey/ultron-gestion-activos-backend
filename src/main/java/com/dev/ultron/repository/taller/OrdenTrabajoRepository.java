package com.dev.ultron.repository.taller;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {

    @Query("SELECT ot FROM OrdenTrabajo ot " +
           "LEFT JOIN ot.cliente c LEFT JOIN c.persona p " +
           "LEFT JOIN ot.vehiculo v " +
           "WHERE LOWER(ot.numeroOrden) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(ot.etapa) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<OrdenTrabajo> search(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT ot FROM OrdenTrabajo ot WHERE ot.cliente.id_cliente = :idCliente ORDER BY ot.fechaCreacion DESC")
    Page<OrdenTrabajo> findByClienteId(@Param("idCliente") Long idCliente, Pageable pageable);

    @Query("SELECT ot FROM OrdenTrabajo ot WHERE ot.vehiculo.id_bien = :idVehiculo ORDER BY ot.fechaCreacion DESC")
    Page<OrdenTrabajo> findByVehiculoId(@Param("idVehiculo") Long idVehiculo, Pageable pageable);

    @Query("""
            SELECT ot FROM OrdenTrabajo ot
            WHERE ot.mecanico.id_funcionario = :idMecanico
              AND COALESCE(ot.fechaInicioEstimada, ot.fechaCreacion) >= :desde
              AND COALESCE(ot.fechaInicioEstimada, ot.fechaCreacion) <= :hasta
            ORDER BY COALESCE(ot.fechaInicioEstimada, ot.fechaCreacion)
            """)
    List<OrdenTrabajo> findAgendaMecanico(
            @Param("idMecanico") Long idMecanico,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query(value = "SELECT NEXTVAL('taller.orden_trabajo_numero_seq')", nativeQuery = true)
    Long obtenerSiguienteNumero();
}
