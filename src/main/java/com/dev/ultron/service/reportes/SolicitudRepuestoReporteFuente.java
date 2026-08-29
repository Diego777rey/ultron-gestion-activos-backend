package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.taller.SolicitudRepuesto;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.taller.SolicitudRepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SolicitudRepuestoReporteFuente implements ReporteFuente {

    private final SolicitudRepuestoRepository solicitudRepuestoRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.SOLICITUD_REPUESTO;
    }

    @Override
    public String titulo() {
        return "Solicitudes de repuestos";
    }

    @Override
    public String subtitulo() {
        return "Pedidos de materiales entre sectores";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "ÍTEMS / OT";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-solicitudes-repuesto.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<SolicitudRepuesto> solicitudes = (filtro != null && !filtro.isBlank())
                ? solicitudRepuestoRepository.buscarParaReporte(filtro.trim())
                : solicitudRepuestoRepository.findAllParaReporte();
        return mapear(solicitudes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        SolicitudRepuesto solicitud = solicitudRepuestoRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud de repuesto no encontrada con id: " + id));
        return mapear(List.of(solicitud));
    }

    private List<ReporteFila> mapear(List<SolicitudRepuesto> solicitudes) {
        AtomicInteger numero = new AtomicInteger(1);
        return solicitudes.stream()
                .map(solicitud -> ReporteFilaMapper.deSolicitudRepuesto(solicitud, numero.getAndIncrement()))
                .toList();
    }
}
