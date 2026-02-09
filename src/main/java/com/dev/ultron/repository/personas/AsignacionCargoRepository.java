package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.AsignacionCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionCargoRepository extends JpaRepository<AsignacionCargo, Long> {
}
