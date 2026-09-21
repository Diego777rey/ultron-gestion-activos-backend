package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteTransferenciaDetalleMapper;
import com.dev.ultron.dto.reportes.output.ReporteTransferenciaDetalleFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.operaciones.TransferenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransferenciaDetalleReporteFuente implements ReporteFuente {

    private final TransferenciaRepository transferenciaRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.TRANSFERENCIA_DETALLE;
    }

    @Override
    public String titulo() {
        return "Comprobante de transferencia";
    }

    @Override
    public String subtitulo() {
        return "Movimiento de stock entre sectores — para firma de responsables";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "";
    }

    @Override
    public String nombreArchivo() {
        return "comprobante-transferencia.pdf";
    }

    @Override
    public String plantilla() {
        return JasperReportService.PLANTILLA_TRANSFERENCIA_DETALLE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteTransferenciaDetalleFila> listar(String filtro) {
        List<Transferencia> transferencias = (filtro != null && !filtro.isBlank())
                ? transferenciaRepository.buscarParaReporte(filtro.trim())
                : transferenciaRepository.findAllParaReporte();
        return mapear(transferencias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteTransferenciaDetalleFila> buscarPorId(Long id) {
        Transferencia transferencia = transferenciaRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Transferencia no encontrada con id: " + id));
        return mapear(List.of(transferencia));
    }

    private List<ReporteTransferenciaDetalleFila> mapear(List<Transferencia> transferencias) {
        List<ReporteTransferenciaDetalleFila> filas = new ArrayList<>();
        for (Transferencia transferencia : transferencias) {
            if (transferencia.getDetalles() != null) {
                transferencia.getDetalles().size();
            }
            filas.addAll(ReporteTransferenciaDetalleMapper.deTransferencia(transferencia));
        }
        return filas;
    }
}
