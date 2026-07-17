package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
}
