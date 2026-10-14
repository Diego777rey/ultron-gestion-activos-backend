package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFilaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        return "Listado de facturas legales";
    }

    @Override
    public String subtitulo() {
        return "Facturas emitidas con venta asociada";
    }

    @Override
    public String etiquetaColumnaExtra() {
        return "FORMA PAGO";
    }

    @Override
    public String nombreArchivo() {
        return "reporte-facturas.pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro) {
        throw new IllegalArgumentException("Debe indicar la empresa para el reporte de facturas.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> listar(String filtro, Long idEmpresa) {
        if (idEmpresa == null) {
            throw new IllegalArgumentException("Debe indicar la empresa para el reporte de facturas.");
        }
        List<Factura> facturas = (filtro != null && !filtro.isBlank())
                ? facturaRepository.searchFacturasConVentaParaReporte(idEmpresa, filtro.trim())
                : facturaRepository.findFacturasConVentaParaReporte(idEmpresa, ESTADO_EMITIDA);
        return mapear(facturas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFila> buscarPorId(Long id) {
        Factura factura = facturaRepository.findParaReporte(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con id: " + id));
        return mapear(List.of(factura));
    }

    private List<ReporteFila> mapear(List<Factura> facturas) {
        AtomicInteger numero = new AtomicInteger(1);
        return facturas.stream()
                .map(factura -> ReporteFilaMapper.deFactura(factura, numero.getAndIncrement()))
                .toList();
    }
}
