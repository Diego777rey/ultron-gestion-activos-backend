package com.dev.ultron.repository.financiero;

import com.dev.ultron.domain.financiero.Timbrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimbradoRepository extends JpaRepository<Timbrado, Long> {

    @Query("SELECT t FROM Timbrado t WHERE t.numero_timbrado = :numeroTimbrado")
    Optional<Timbrado> findByNumero_timbrado(@Param("numeroTimbrado") String numeroTimbrado);

    @Query("""
            SELECT t FROM Timbrado t
            WHERE t.empresa.id_empresa = :idEmpresa
            AND t.activo = true
            ORDER BY t.fecha_inicio_vigencia DESC
            """)
    List<Timbrado> findActivosByEmpresa(@Param("idEmpresa") Long idEmpresa);

    @Query("""
            SELECT t FROM Timbrado t
            WHERE t.empresa.id_empresa = :idEmpresa
            AND t.activo = true
            AND CURRENT_DATE BETWEEN t.fecha_inicio_vigencia AND t.fecha_fin_vigencia
            ORDER BY t.fecha_inicio_vigencia DESC
            """)
    List<Timbrado> findVigentesByEmpresa(@Param("idEmpresa") Long idEmpresa);

    @Query("""
            SELECT t FROM Timbrado t
            WHERE t.empresa.id_empresa = :idEmpresa
            AND t.activo = true
            AND CURRENT_DATE BETWEEN t.fecha_inicio_vigencia AND t.fecha_fin_vigencia
            AND t.numero_actual <= t.numero_final
            ORDER BY t.fecha_inicio_vigencia DESC
            LIMIT 1
            """)
    Optional<Timbrado> findTimbradoActivoDisponible(@Param("idEmpresa") Long idEmpresa);

    @Query("""
            SELECT t FROM Timbrado t
            WHERE t.empresa.id_empresa = :idEmpresa
            ORDER BY t.fecha_creacion DESC
            """)
    List<Timbrado> findAllByEmpresa(@Param("idEmpresa") Long idEmpresa);

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END 
            FROM Timbrado t
            WHERE t.numero_timbrado = :numeroTimbrado
            AND t.establecimiento = :establecimiento
            AND t.punto_expedicion = :puntoExpedicion
            """)
    boolean existsByNumeroAndEstablecimientoAndPunto(
            @Param("numeroTimbrado") String numeroTimbrado,
            @Param("establecimiento") String establecimiento,
            @Param("puntoExpedicion") String puntoExpedicion
    );

    @Query("""
            SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
            FROM Timbrado t
            WHERE t.numero_timbrado = :numeroTimbrado
            AND t.establecimiento = :establecimiento
            AND t.punto_expedicion = :puntoExpedicion
            AND t.id_timbrado <> :idExcluir
            """)
    boolean existsOtroConMismoPunto(
            @Param("numeroTimbrado") String numeroTimbrado,
            @Param("establecimiento") String establecimiento,
            @Param("puntoExpedicion") String puntoExpedicion,
            @Param("idExcluir") Long idExcluir
    );
}
