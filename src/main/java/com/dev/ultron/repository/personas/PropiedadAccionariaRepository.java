package com.dev.ultron.repository.personas;

import com.dev.ultron.domain.personas.PropiedadAccionaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropiedadAccionariaRepository extends JpaRepository<PropiedadAccionaria, Long> {
}
