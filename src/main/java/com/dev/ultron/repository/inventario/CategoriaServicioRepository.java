package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Long>, JpaSpecificationExecutor<CategoriaServicio> {
}
