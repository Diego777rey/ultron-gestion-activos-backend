package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFacturaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFacturaFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FacturaReporteFuente implements ReporteFuenteParametrizada {

    private static final String ESTADO_EMITIDA = "EMITIDA";

    private final FacturaRepository facturaRepository;

    @Override
    public TipoReporte tipo() {
        return TipoReporte.FACTURA;
    }

    @Override
    public String titulo() {
        return "Facturas legales";
    }

    @Override
    public String subtitulo() {
        return "Número, timbrado, IVA y total";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-facturas.pdf";
    }

    @Override
    public String plantilla() {
        return JasperReportService.PLANTILLA_FACTURAS;
    }

    @Override
    public List<ReporteFacturaFila> listar(String filtro) {
        throw new IllegalArgumentException("Debe indicar la empresa para el reporte de facturas.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFacturaFila> listar(String filtro, Long idEmpresa) {
        if (idEmpresa == null) {
            throw new IllegalArgumentException("Debe indicar la empresa para el reporte de facturas.");
        }
        List<Factura> facturas = (filtro != null && !filtro.isBlank())
                ? facturaRepository.searchFacturasConVentaParaReporte(idEmpresa, filtro.trim())
                : facturaRepository.findFacturasConVentaParaReporte(idEmpresa, ESTADO_EMITIDA);
        return facturas.stream().map(ReporteFacturaMapper::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFacturaFila> buscarPorId(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con id: " + id));
        return List.of(ReporteFacturaMapper.de(factura));
    }
}
