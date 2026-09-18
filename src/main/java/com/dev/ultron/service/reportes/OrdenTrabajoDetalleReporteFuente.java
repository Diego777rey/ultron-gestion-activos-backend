package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteOtDetalleMapper;
import com.dev.ultron.dto.reportes.output.ReporteOtDetalleFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrdenTrabajoDetalleReporteFuente implements ReporteFuente {

    private final OrdenTrabajoRepository ordenTrabajoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.ORDEN_TRABAJO_DETALLE;
    }

    @Override
    public String titulo() {
        return "Detalle de órdenes de trabajo";
    }

    @Override
    public String subtitulo() {
        return "Vista completa de recepción, diagnóstico y líneas";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-orden-trabajo-detalle.pdf";
    }

    @Override
    public String plantilla() {
        return JasperReportService.PLANTILLA_ORDEN_DETALLE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteOtDetalleFila> listar(String filtro) {
        List<OrdenTrabajo> ordenes = (filtro != null && !filtro.isBlank())
                ? ordenTrabajoRepository.buscarParaReporteDetalle(filtro.trim())
                : ordenTrabajoRepository.findAllParaReporteDetalle();
        return mapear(ordenes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteOtDetalleFila> buscarPorId(Long id) {
        OrdenTrabajo orden = ordenTrabajoRepository.findParaReporteDetalle(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada con id: " + id));
        return mapear(List.of(orden));
    }

    private List<ReporteOtDetalleFila> mapear(List<OrdenTrabajo> ordenes) {
        Map<Long, OrdenTrabajo> unicas = new LinkedHashMap<>();
        for (OrdenTrabajo orden : ordenes) {
            unicas.putIfAbsent(orden.getId_orden_trabajo(), orden);
        }
        List<ReporteOtDetalleFila> filas = new ArrayList<>();
        for (OrdenTrabajo orden : unicas.values()) {
            if (orden.getHallazgos() != null) {
                orden.getHallazgos().size();
            }
            filas.addAll(ReporteOtDetalleMapper.deOrden(orden));
        }
        return filas;
    }
}
