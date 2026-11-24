package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Caja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    @Query("""
            SELECT c FROM Caja c
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR CONCAT(c.id_caja, '') LIKE CONCAT('%', :filter, '%')
                OR LOWER(COALESCE(c.mac_address, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(c.nombre_terminal, '')) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<Caja> buscar(@Param("filter") String filter, Pageable pageable);

    @Query("""
            SELECT c FROM Caja c
            WHERE c.activa = TRUE
              AND NOT EXISTS (
                  SELECT 1 FROM SesionCaja s
                  WHERE s.caja = c AND s.estado = 'ABIERTA'
              )
            ORDER BY c.nombre
            """)
    List<Caja> findDisponibles();

    @Query("""
            SELECT c FROM Caja c
            WHERE c.activa = TRUE
              AND c.mac_address IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM SesionCaja s
                  WHERE s.caja = c AND s.estado = 'ABIERTA'
              )
            ORDER BY c.nombre
            """)
    List<Caja> findDisponiblesSinVinculo();

    @Query("SELECT c FROM Caja c WHERE c.mac_address = :mac")
    Optional<Caja> findByMacAddress(@Param("mac") String macAddress);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM Caja c
            WHERE c.mac_address = :mac
              AND (:excludeId IS NULL OR c.id_caja <> :excludeId)
            """)
    boolean existsByMacAddressExcluding(
            @Param("mac") String macAddress,
            @Param("excludeId") Long excludeId);
}
