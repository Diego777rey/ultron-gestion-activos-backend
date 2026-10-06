package com.dev.ultron.repository.patrimonio;

import com.dev.ultron.domain.patrimonio.Equipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    @Query("SELECT e FROM Equipo e ORDER BY e.id_equipo DESC")
    Page<Equipo> findAllOrdenados(Pageable pageable);

    @Query("""
            SELECT e FROM Equipo e
            LEFT JOIN e.cliente c LEFT JOIN c.persona p
            LEFT JOIN e.vehiculo v
            WHERE LOWER(e.tipoEquipo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.marca) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.modelo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.numeroSerie) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(p.documento) LIKE LOWER(CONCAT('%', :filter, '%'))
            ORDER BY e.id_equipo DESC
            """)
    Page<Equipo> search(@Param("filter") String filter, Pageable pageable);

    @Query("SELECT e FROM Equipo e WHERE e.cliente.id_cliente = :idCliente ORDER BY e.id_equipo DESC")
    Page<Equipo> findByClienteId(@Param("idCliente") Long idCliente, Pageable pageable);

    @Query("""
            SELECT e FROM Equipo e
            LEFT JOIN e.vehiculo v
            WHERE e.cliente.id_cliente = :idCliente
              AND (LOWER(e.tipoEquipo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.marca) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.modelo) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(e.numeroSerie) LIKE LOWER(CONCAT('%', :filter, '%'))
                OR LOWER(v.chapa) LIKE LOWER(CONCAT('%', :filter, '%')))
            ORDER BY e.id_equipo DESC
            """)
    Page<Equipo> searchByClienteId(@Param("idCliente") Long idCliente, @Param("filter") String filter, Pageable pageable);
}
