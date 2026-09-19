package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.operaciones.TransferenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class TransferenciaReporteFuente implements ReporteFuente {

    private final TransferenciaRepository transferenciaRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.TRANSFERENCIA;
    }

    @Override
    public String titulo() {
        return "Listado de transferencias";
    }

    @Override
    public String subtitulo() {
        return "Movimientos de stock entre sectores";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "ÍTEMS";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-transferencias.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        List<Transferencia> transferencias = (filtro != null && !filtro.isBlank())
                ? transferenciaRepository.buscarParaReporte(filtro.trim())
                : transferenciaRepository.findAllParaReporte();
        return mapear(transferencias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Transferencia transferencia = transferenciaRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Transferencia no encontrada con id: " + id));
        return mapear(List.of(transferencia));
    }

    private List<ReporteFila> mapear(List<Transferencia> transferencias) {
        Map<Long, Integer> itemsPorId = contarItems(transferencias);
        AtomicInteger numero = new AtomicInteger(1);
        return transferencias.stream()
                .map(transferencia -> ReporteFilaMapper.deTransferencia(
                        transferencia,
                        numero.getAndIncrement(),
                        itemsPorId.getOrDefault(transferencia.getId_transferencia(), 0)))
                .toList();
    }

    private Map<Long, Integer> contarItems(List<Transferencia> transferencias) {
        List<Long> ids = ReporteConteos.ids(transferencias.stream()
                .map(Transferencia::getId_transferencia)
                .toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return ReporteConteos.deFilas(transferenciaRepository.contarDetallesParaReporte(ids));
    }
}
