package com.dev.ultron.repository.patrimonio;

import com.dev.ultron.domain.patrimonio.PropiedadBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropiedadBienRepository extends JpaRepository<PropiedadBien, Long> {
}
