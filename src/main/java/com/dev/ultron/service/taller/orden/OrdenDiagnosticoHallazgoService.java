package com.dev.ultron.service.taller.orden;

import com.dev.ultron.domain.taller.OrdenDiagnosticoHallazgo;
import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.taller.input.OrdenDiagnosticoHallazgoInput;
import com.dev.ultron.dto.taller.mapper.OrdenTrabajoMapper;
import com.dev.ultron.dto.taller.output.OrdenTrabajoOutput;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Gestiona fallos y defectos encontrados en el diagnóstico.
 */
@Service
public class OrdenDiagnosticoHallazgoService {

    private static final Set<String> TIPOS = Set.of("FALLO", "DEFECTO");
    private static final Set<String> GRAVEDADES = Set.of("BAJA", "MEDIA", "ALTA", "CRITICA");

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;

    public OrdenDiagnosticoHallazgoService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenTrabajoMapper ordenTrabajoMapper) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
    }

    @Transactional
    public OrdenTrabajoOutput agregarHallazgo(Long idOrden, OrdenDiagnosticoHallazgoInput input) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        exigirEtapaEditable(orden.getEtapa());
        validarInput(input);

        orden.ensureDiagnostico();
        if (orden.getHallazgos() == null) {
            orden.setHallazgos(new ArrayList<>());
        }

        String tipo = input.tipo().trim().toUpperCase();
        if ("EN_PROCESO".equalsIgnoreCase(orden.getEtapa()) && !"DEFECTO".equals(tipo)) {
            throw new IllegalArgumentException(
                    "En En Proceso solo se puede registrar un defecto descubierto durante el trabajo");
        }

        OrdenDiagnosticoHallazgo hallazgo = OrdenDiagnosticoHallazgo.builder()
                .ordenTrabajo(orden)
                .tipo(tipo)
                .gravedad(resolverGravedad(input.gravedad()))
                .sistema(normalizarTexto(input.sistema()))
                .descripcion(input.descripcion().trim().toUpperCase())
                .etapaOrigen(orden.getEtapa())
                .build();

        orden.getHallazgos().add(hallazgo);
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    @Transactional
    public OrdenTrabajoOutput eliminarHallazgo(Long idOrden, Long idHallazgo) {
        OrdenTrabajo orden = buscarPorIdOrThrow(idOrden);
        exigirEtapaEditable(orden.getEtapa());

        OrdenDiagnosticoHallazgo hallazgo = (orden.getHallazgos() == null ? List.<OrdenDiagnosticoHallazgo>of() : orden.getHallazgos())
                .stream()
                .filter(h -> h.getId_hallazgo().equals(idHallazgo))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Hallazgo no encontrado con ID: " + idHallazgo));

        if ("EN_PROCESO".equalsIgnoreCase(orden.getEtapa())
                && !"EN_PROCESO".equalsIgnoreCase(hallazgo.getEtapaOrigen())) {
            throw new IllegalArgumentException(
                    "No se pueden eliminar hallazgos del diagnóstico durante En Proceso");
        }

        orden.getHallazgos().remove(hallazgo);
        return ordenTrabajoMapper.toOutput(ordenTrabajoRepository.save(orden));
    }

    private void validarInput(OrdenDiagnosticoHallazgoInput input) {
        if (input == null) {
            throw new IllegalArgumentException("El hallazgo es obligatorio");
        }
        if (input.tipo() == null || input.tipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de hallazgo es obligatorio (FALLO o DEFECTO)");
        }
        String tipo = input.tipo().trim().toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de hallazgo inválido: " + input.tipo()
                    + ". Debe ser FALLO o DEFECTO.");
        }
        if (input.gravedad() != null && !input.gravedad().isBlank()) {
            String gravedad = input.gravedad().trim().toUpperCase();
            if (!GRAVEDADES.contains(gravedad)) {
                throw new IllegalArgumentException("Gravedad inválida: " + input.gravedad()
                        + ". Debe ser BAJA, MEDIA, ALTA o CRITICA.");
            }
        }
        if (input.descripcion() == null || input.descripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del hallazgo es obligatoria");
        }
    }

    private String resolverGravedad(String gravedad) {
        if (gravedad == null || gravedad.isBlank()) {
            return "MEDIA";
        }
        return gravedad.trim().toUpperCase();
    }

    private String normalizarTexto(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private void exigirEtapaEditable(String etapa) {
        if (!"DIAGNOSTICO".equalsIgnoreCase(etapa) && !"EN_PROCESO".equalsIgnoreCase(etapa)) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar hallazgos en DIAGNOSTICO o EN_PROCESO. Etapa actual: " + etapa);
        }
    }

    private OrdenTrabajo buscarPorIdOrThrow(Long id) {
        return ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada: " + id));
    }
}
