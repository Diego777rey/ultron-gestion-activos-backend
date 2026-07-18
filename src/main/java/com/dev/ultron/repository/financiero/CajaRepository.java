package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Caja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    @Query("""
            SELECT c FROM Caja c
            LEFT JOIN c.sector s
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<Caja> buscar(@Param("filter") String filter, Pageable pageable);
}
