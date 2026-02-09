package com.dev.ultron.repository.patrimonio;

import com.dev.ultron.domain.patrimonio.Bien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long> {
}
