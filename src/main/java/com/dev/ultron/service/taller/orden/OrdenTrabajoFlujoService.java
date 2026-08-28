package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.taller.mapper.OrdenTrabajoMapper;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gestiona el flujo de etapas: avance, envío a caja y facturación.
 */
@Service
public class OrdenTrabajoFlujoService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;
    private final OrdenTrabajoEtapaValidator etapaValidator;
    private final OrdenTrabajoCajaResolver cajaResolver;

    public OrdenTrabajoFlujoService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoMapper ordenTrabajoMapper,
            OrdenTrabajoEtapaValidator etapaValidator,
            OrdenTrabajoCajaResolver cajaResolver) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
        this.etapaValidator = etapaValidator;
        this.cajaResolver = cajaResolver;
    }

    @Transactional
    public OrdenTrabajoOutput cambiarEtapa(Long id, String nuevaEtapa) {
        OrdenTrabajo orden = buscarPorIdOrThrow(id);
        String destino = nuevaEtapa.toUpperCase();
        etapaValidator.validarTransicion(orden.getEtapa(), destino);
        etapaValidator.validarRequisitos(orden, destino);

        orden.setEtapa(destino);
        if ("FINALIZADA".equals(destino)) {
            orden.setFechaFinalizacion(LocalDateTime.now());
        }

        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    @Transactional
    public OrdenTrabajoOutput enviarACaja(Long idOrden, Long idCaja) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        if (!"EN_PROCESO".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se puede enviar a caja una orden en etapa EN_PROCESO. Etapa actual: " + orden.getEtapa());
        }
        Caja caja = cajaResolver.exigirConSesionAbierta(idCaja);
        orden.setCaja(caja);
        orden.setEtapa("FINALIZADA");
        orden.setFechaFinalizacion(LocalDateTime.now());
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    @Transactional
    public OrdenTrabajoOutput marcarFacturada(Long idOrden) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        if (!"FINALIZADA".equalsIgnoreCase(orden.getEtapa())) {
            throw new IllegalArgumentException(
                    "Solo se puede facturar una orden FINALIZADA. Etapa actual: " + orden.getEtapa());
        }
        orden.setEtapa("FACTURADO");
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    private OrdenTrabajo buscarPorIdOrThrow(Long id) {
        return ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada: " + id));
    }
}
