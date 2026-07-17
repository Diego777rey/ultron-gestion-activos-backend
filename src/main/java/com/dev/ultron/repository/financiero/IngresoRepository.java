package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
}
