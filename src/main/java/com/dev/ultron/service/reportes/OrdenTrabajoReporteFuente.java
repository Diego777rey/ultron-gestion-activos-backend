package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.taller.OrdenTrabajo;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.taller.OrdenTrabajoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class OrdenTrabajoReporteFuente implements ReporteFuente {

    private final OrdenTrabajoRepository ordenTrabajoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.ORDEN_TRABAJO;
    }

    @Override
    public String titulo() {
        return "Órdenes de trabajo";
    }

    @Override
    public String subtitulo() {
        return "Listado operativo del taller";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "CHAPA";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-ordenes-trabajo.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<OrdenTrabajo> ordenes = (filtro != null && !filtro.isBlank())
                ? ordenTrabajoRepository.buscarParaReporte(filtro.trim())
                : ordenTrabajoRepository.findAllParaReporte();
        return mapear(ordenes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        OrdenTrabajo orden = ordenTrabajoRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de trabajo no encontrada con id: " + id));
        return mapear(List.of(orden));
    }

    private List<ReporteFila> mapear(List<OrdenTrabajo> ordenes) {
        AtomicInteger numero = new AtomicInteger(1);
        return ordenes.stream()
                .map(orden -> ReporteFilaMapper.deOrdenTrabajo(orden, numero.getAndIncrement()))
                .toList();
    }
}
