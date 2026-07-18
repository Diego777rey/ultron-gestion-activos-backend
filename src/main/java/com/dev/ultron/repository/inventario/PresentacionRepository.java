package com.dev.ultron.repository.inventario;

import com.dev.ultron.domain.inventario.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PresentacionRepository extends JpaRepository<Presentacion, Long>, JpaSpecificationExecutor<Presentacion> {
}
