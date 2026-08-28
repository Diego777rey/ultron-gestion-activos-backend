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
            LEFT JOIN ot.diagnostico d
            WHERE ot.mecanico.id_funcionario = :idMecanico
              AND COALESCE(d.fechaInicioEstimada, ot.fechaCreacion) >= :desde
              AND COALESCE(d.fechaInicioEstimada, ot.fechaCreacion) <= :hasta
            ORDER BY COALESCE(d.fechaInicioEstimada, ot.fechaCreacion)
            """)
    List<OrdenTrabajo> findAgendaMecanico(
            @Param("idMecanico") Long idMecanico,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query(value = "SELECT NEXTVAL('taller.orden_trabajo_numero_seq')", nativeQuery = true)
    Long obtenerSiguienteNumero();

    @Query("""
            SELECT DISTINCT ot FROM OrdenTrabajo ot
            LEFT JOIN FETCH ot.cliente c
            LEFT JOIN FETCH c.persona
            LEFT JOIN FETCH ot.vehiculo
            LEFT JOIN FETCH ot.sector
            LEFT JOIN FETCH ot.recepcion
            LEFT JOIN FETCH ot.diagnostico
            ORDER BY ot.fechaCreacion DESC
            """)
    java.util.List<OrdenTrabajo> findAllParaReporte();

    @Query("""
            SELECT DISTINCT ot FROM OrdenTrabajo ot
            LEFT JOIN FETCH ot.cliente c
            LEFT JOIN FETCH c.persona p
            LEFT JOIN FETCH ot.vehiculo v
            LEFT JOIN FETCH ot.sector
            LEFT JOIN FETCH ot.recepcion
            LEFT JOIN FETCH ot.diagnostico
            WHERE LOWER(ot.numeroOrden) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(ot.etapa) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY ot.fechaCreacion DESC
            """)
    java.util.List<OrdenTrabajo> buscarParaReporte(@Param("filter") String filter);

    @Query("""
            SELECT ot FROM OrdenTrabajo ot
            LEFT JOIN FETCH ot.cliente c
            LEFT JOIN FETCH c.persona
            LEFT JOIN FETCH ot.vehiculo
            LEFT JOIN FETCH ot.sector
            LEFT JOIN FETCH ot.recepcion
            LEFT JOIN FETCH ot.diagnostico
            WHERE ot.id_orden_trabajo = :id
            """)
    java.util.Optional<OrdenTrabajo> findParaReporte(@Param("id") Long id);
}
