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
public class HistorialReporteFuente implements ReporteFuente {

    private final OrdenTrabajoRepository ordenTrabajoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.HISTORIAL;
    }

    @Override
    public String titulo() {
        return "Historial de órdenes de trabajo";
    }

    @Override
    public String subtitulo() {
        return "Registro histórico del taller";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "FINALIZACIÓN";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-historial.pdf";
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
                .map(orden -> ReporteFilaMapper.deOrdenTrabajoHistorial(orden, numero.getAndIncrement()))
                .toList();
    }
}
