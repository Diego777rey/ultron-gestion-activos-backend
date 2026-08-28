package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.inventario.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ServicioReporteFuente implements ReporteFuente {

    private final ServicioRepository servicioRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.SERVICIO;
    }

    @Override
    public String titulo() {
        return "Catálogo de servicios";
    }

    @Override
    public String subtitulo() {
        return "Listado genérico de inventario";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "DETALLE";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-servicios.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Servicio> servicios;
        if (filtro != null && !filtro.isBlank()) {
            servicios = servicioRepository.buscarParaReporte(filtro.trim());
        } else {
            servicios = servicioRepository.findAllParaReporte();
        }
        return mapear(servicios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Servicio servicio = servicioRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con id: " + id));
        return mapear(List.of(servicio));
    }

    private List<ReporteFila> mapear(List<Servicio> servicios) {
        AtomicInteger numero = new AtomicInteger(1);
        return servicios.stream()
                .map(servicio -> ReporteFilaMapper.deServicio(servicio, numero.getAndIncrement()))
                .toList();
    }
}
