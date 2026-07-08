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
}
