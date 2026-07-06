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

    Page<Vehiculo> findByClienteId_cliente(Long idCliente, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehiculo v "
            + "WHERE UPPER(v.chapa) = UPPER(:chapa) AND (:idExcluir IS NULL OR v.id_bien <> :idExcluir)")
    boolean existsByChapaExcludingId(@Param("chapa") String chapa, @Param("idExcluir") Long idExcluir);

    Optional<Vehiculo> findByChapaIgnoreCase(String chapa);
}
