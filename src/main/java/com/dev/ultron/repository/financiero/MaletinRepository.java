package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Maletin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaletinRepository extends JpaRepository<Maletin, Long> {

    @Query("""
            SELECT m FROM Maletin m
            WHERE (:filter IS NULL OR :filter = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<Maletin> buscar(@Param("filter") String filter, Pageable pageable);

    List<Maletin> findByActivoTrueAndEstado(String estado);
}
