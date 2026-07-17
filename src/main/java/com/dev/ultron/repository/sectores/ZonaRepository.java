package com.dev.ultron.repository.sectores;

import com.dev.ultron.domain.sectores.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long>, JpaSpecificationExecutor<Zona> {

    @Query("SELECT z FROM Zona z WHERE z.sector.id_sector = :idSector ORDER BY z.nombre")
    List<Zona> findBySectorId(@Param("idSector") Long idSector);

    @Query("SELECT CASE WHEN COUNT(z) > 0 THEN true ELSE false END FROM Zona z WHERE z.sector.id_sector = :idSector")
    boolean existsBySectorId(@Param("idSector") Long idSector);
}
