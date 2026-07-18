package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.SesionCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    @Query("SELECT s FROM SesionCaja s WHERE s.estado = :estado ORDER BY s.fechaApertura DESC")
    List<SesionCaja> listarPorEstado(@Param("estado") String estado, Pageable pageable);

    @Query("SELECT s FROM SesionCaja s WHERE s.caja.id_caja = :idCaja AND s.estado = :estado")
    Optional<SesionCaja> findPorCajaYEstado(@Param("idCaja") Long idCaja, @Param("estado") String estado);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SesionCaja s WHERE s.caja.id_caja = :idCaja AND s.estado = :estado")
    boolean existsPorCajaYEstado(@Param("idCaja") Long idCaja, @Param("estado") String estado);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SesionCaja s WHERE s.maletin.id_maletin = :idMaletin AND s.estado = :estado")
    boolean existsPorMaletinYEstado(@Param("idMaletin") Long idMaletin, @Param("estado") String estado);

    @Query("SELECT s FROM SesionCaja s WHERE s.maletin.id_maletin = :idMaletin AND s.estado = 'ABIERTA'")
    Optional<SesionCaja> findAbiertaPorMaletin(@Param("idMaletin") Long idMaletin);

    @Query("""
            SELECT s FROM SesionCaja s
            WHERE s.maletin.id_maletin = :idMaletin
            ORDER BY COALESCE(s.fechaCierre, s.fechaApertura) DESC
            """)
    List<SesionCaja> findUltimaPorMaletin(@Param("idMaletin") Long idMaletin, Pageable pageable);

    @Query("""
            SELECT s FROM SesionCaja s
            WHERE (:filter IS NULL OR :filter = ''
                OR LOWER(s.caja.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(s.maletin.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))
            """)
    Page<SesionCaja> buscar(@Param("filter") String filter, Pageable pageable);
}
