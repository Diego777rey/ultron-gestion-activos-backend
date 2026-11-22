package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Terminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    @Query("SELECT t FROM Terminal t WHERE t.mac_address = :mac")
    Optional<Terminal> findByMacAddress(@Param("mac") String macAddress);

    @Query("SELECT t FROM Terminal t WHERE t.caja.id_caja = :idCaja")
    Optional<Terminal> findByCajaId(@Param("idCaja") Long idCaja);

    @Query("""
            SELECT t FROM Terminal t
            LEFT JOIN t.caja c
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(COALESCE(t.mac_address, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(t.hostname, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR CONCAT(t.id_terminal, '') LIKE CONCAT('%', :filter, '%'))
            """)
    Page<Terminal> buscar(@Param("filter") String filter, Pageable pageable);
}
