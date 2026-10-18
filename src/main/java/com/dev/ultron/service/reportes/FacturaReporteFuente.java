package com.dev.ultron.service.reportes;

import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.dto.reportes.TipoReporte;
import com.dev.ultron.dto.reportes.mapper.ReporteFacturaMapper;
import com.dev.ultron.dto.reportes.output.ReporteFacturaFila;
import com.dev.ultron.generic.EntityNotFoundException;
import com.dev.ultron.repository.financiero.FacturaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FacturaReporteFuente implements ReporteFuenteParametrizada {

    private static final String ESTADO_EMITIDA = "EMITIDA";

    private final FacturaRepository facturaRepository;

    public FacturaReporteFuente(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

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
        return listar(filtro, idEmpresa, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFacturaFila> listar(String filtro, Long idEmpresa, String fechaDesde, String fechaHasta) {
        if (idEmpresa == null) {
            throw new IllegalArgumentException("Debe indicar la empresa para el reporte de facturas.");
        }
        LocalDateTime desde = inicioDia(fechaDesde);
        LocalDateTime hasta = finDia(fechaHasta);
        List<Factura> facturas = (filtro != null && !filtro.isBlank())
                ? facturaRepository.searchFacturasConVentaParaReporte(idEmpresa, filtro.trim(), desde, hasta)
                : facturaRepository.findFacturasConVentaParaReporte(idEmpresa, ESTADO_EMITIDA, desde, hasta);
        return facturas.stream().map(ReporteFacturaMapper::de).toList();
    }

    private static LocalDateTime inicioDia(String value) {
        LocalDate date = parseIsoDate(value);
        return date != null ? date.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
    }

    private static LocalDateTime finDia(String value) {
        LocalDate date = parseIsoDate(value);
        return date != null ? date.atTime(23, 59, 59) : LocalDateTime.of(2999, 12, 31, 23, 59, 59);
    }

    private static LocalDate parseIsoDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String iso = value.length() >= 10 ? value.substring(0, 10) : value;
        return java.time.LocalDate.parse(iso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteFacturaFila> buscarPorId(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con id: " + id));
        return List.of(ReporteFacturaMapper.de(factura));
    }
}
