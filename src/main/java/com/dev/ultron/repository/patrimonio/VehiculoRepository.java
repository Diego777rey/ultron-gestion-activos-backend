package com.dev.ultron.repository.patrimonio;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query("SELECT v FROM Vehiculo v WHERE v.cliente.id_cliente = :idCliente")
    Page<Vehiculo> findByClienteId(@Param("idCliente") Long idCliente, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehiculo v "
            + "WHERE UPPER(v.chapa) = UPPER(:chapa) AND (:idExcluir IS NULL OR v.id_bien <> :idExcluir)")
    boolean existsByChapaExcludingId(@Param("chapa") String chapa, @Param("idExcluir") Long idExcluir);

    Optional<Vehiculo> findByChapaIgnoreCase(String chapa);

    @Query("SELECT v FROM Vehiculo v WHERE LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(v.modelo) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Vehiculo> search(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT v FROM Vehiculo v WHERE v.cliente.id_cliente = :idCliente AND (LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :filter, '%')) OR LOWER(v.modelo) LIKE LOWER(CONCAT('%', :filter, '%')))")
    Page<Vehiculo> searchByClienteId(@Param("idCliente") Long idCliente, @Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT DISTINCT v FROM Vehiculo v
            LEFT JOIN FETCH v.cliente c
            LEFT JOIN FETCH c.persona
            ORDER BY v.marca, v.modelo, v.chapa
            """)
    java.util.List<Vehiculo> findAllParaReporte();

    @Query("""
            SELECT DISTINCT v FROM Vehiculo v
            LEFT JOIN FETCH v.cliente c
            LEFT JOIN FETCH c.persona p
            WHERE LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.modelo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.tipoVehiculo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY v.marca, v.modelo, v.chapa
            """)
    java.util.List<Vehiculo> buscarParaReporte(@Param("filter") String filter);

    @Query("""
            SELECT v FROM Vehiculo v
            LEFT JOIN FETCH v.cliente c
            LEFT JOIN FETCH c.persona
            WHERE v.id_bien = :id
            """)
    java.util.Optional<Vehiculo> findParaReporte(@Param("id") Long id);
}
