package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.dto.financiero.mapper.CajaMapper;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.CajaRepository;
import com.dev.ultron.repository.financiero.SesionCajaRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Resuelve cajas con sesión abierta para órdenes de trabajo.
 */
@Component
public class OrdenTrabajoCajaResolver {

    private final CajaRepository cajaRepo;
    private final SesionCajaRepository sesionCajaRepository;
    private final CajaMapper cajaMapper;

    public OrdenTrabajoCajaResolver(
            CajaRepository cajaRepo,
            SesionCajaRepository sesionCajaRepository,
            CajaMapper cajaMapper) {
        this.cajaRepo = cajaRepo;
        this.sesionCajaRepository = sesionCajaRepository;
        this.cajaMapper = cajaMapper;
    }

    public Caja exigirConSesionAbierta(Long idCaja) {
        Caja caja = cajaRepo.findById(idCaja)
                .orElseThrow(() -> new EntityNotFoundException("Caja no encontrada: " + idCaja));
        if (!sesionCajaRepository.existsPorCajaYEstado(idCaja, "ABIERTA")) {
            throw new IllegalArgumentException("La caja no tiene una sesión abierta: " + idCaja);
        }
        return caja;
    }

    public boolean tieneSesionAbierta(Long idCaja) {
        return sesionCajaRepository.existsPorCajaYEstado(idCaja, "ABIERTA");
    }

    public List<CajaOutput> listarConSesionAbierta() {
        return sesionCajaRepository.listarPorEstado("ABIERTA", PageRequest.of(0, 200)).stream()
                .map(SesionCaja::getCaja)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Caja::getId_caja,
                        c -> c,
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values()
                .stream()
                .map(cajaMapper::toOutput)
                .toList();
    }
}
