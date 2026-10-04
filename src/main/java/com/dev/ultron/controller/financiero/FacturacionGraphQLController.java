package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.FacturaInput;
import com.dev.ultron.dto.financiero.input.TimbradoInput;
import com.dev.ultron.dto.financiero.output.FacturaOutput;
import com.dev.ultron.dto.financiero.output.TimbradoOutput;
import com.dev.ultron.service.financiero.FacturaService;
import com.dev.ultron.service.financiero.TimbradoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador GraphQL para operaciones de facturación.
 * Maneja timbrados y facturas.
 */
@Controller
@RequiredArgsConstructor
public class FacturacionGraphQLController {

    private final TimbradoService timbradoService;
    private final FacturaService facturaService;

    // ========================================================================
    // QUERIES - TIMBRADOS
    // ========================================================================

    @QueryMapping
    public TimbradoOutput timbrado(@Argument Long id) {
        return timbradoService.obtenerPorId(id);
    }

    @QueryMapping
    public List<TimbradoOutput> timbrados() {
        return timbradoService.listarTodos().stream()
                .map(timbrado -> timbradoService.obtenerPorId(timbrado.getId_timbrado()))
                .toList();
    }

    @QueryMapping
    public List<TimbradoOutput> timbradosPorEmpresa(@Argument Long idEmpresa) {
        return timbradoService.listarPorEmpresa(idEmpresa);
    }

    @QueryMapping
    public List<TimbradoOutput> timbradosActivosPorEmpresa(@Argument Long idEmpresa) {
        return timbradoService.listarActivosPorEmpresa(idEmpresa);
    }

    @QueryMapping
    public TimbradoOutput timbradoDisponible(@Argument Long idEmpresa) {
        return timbradoService.obtenerTimbradoDisponible(idEmpresa);
    }

    // ========================================================================
    // QUERIES - FACTURAS
    // ========================================================================

    @QueryMapping
    public FacturaOutput factura(@Argument Long id) {
        return facturaService.obtenerPorIdConDetalles(id);
    }

    @QueryMapping
    public FacturaOutput facturaPorVenta(@Argument Long idVenta) {
        return facturaService.obtenerPorVenta(idVenta);
    }

    @QueryMapping
    public Map<String, Object> facturas(@Argument Integer page, @Argument Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(p, s);
        
        Page<FacturaOutput> result = facturaService.listarPaginado(pageable)
                .map(factura -> facturaService.obtenerPorIdConDetalles(factura.getId_factura()));
        
        return buildPageResponse(result);
    }

    @QueryMapping
    public Map<String, Object> facturasPorEmpresaYEstado(
            @Argument Long idEmpresa,
            @Argument String estado,
            @Argument Integer page,
            @Argument Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(p, s);
        
        Page<FacturaOutput> result = facturaService.buscarPorEmpresaYEstado(idEmpresa, estado, pageable);
        
        return buildPageResponse(result);
    }

    @QueryMapping
    public Map<String, Object> facturasPorCliente(
            @Argument Long idCliente,
            @Argument Integer page,
            @Argument Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(p, s);
        
        Page<FacturaOutput> result = facturaService.obtenerPorCliente(idCliente, pageable);
        
        return buildPageResponse(result);
    }

    @QueryMapping
    public Map<String, Object> buscarFacturas(
            @Argument Long idEmpresa,
            @Argument String filtro,
            @Argument Integer page,
            @Argument Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(p, s);
        
        Page<FacturaOutput> result = facturaService.buscarConFiltro(idEmpresa, filtro, pageable);
        
        return buildPageResponse(result);
    }

    @QueryMapping
    public List<FacturaOutput> facturasParaLibroVentas(
            @Argument Long idEmpresa,
            @Argument String fechaInicio,
            @Argument String fechaFin) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime inicio = LocalDateTime.parse(fechaInicio, formatter);
        LocalDateTime fin = LocalDateTime.parse(fechaFin, formatter);
        
        return facturaService.obtenerParaLibroVentas(idEmpresa, inicio, fin);
    }

    // ========================================================================
    // MUTATIONS - TIMBRADOS
    // ========================================================================

    @MutationMapping
    public TimbradoOutput registrarTimbrado(@Argument TimbradoInput input) {
        return timbradoService.registrarTimbrado(input);
    }

    @MutationMapping
    public TimbradoOutput actualizarTimbrado(@Argument Long id, @Argument TimbradoInput input) {
        return timbradoService.actualizarTimbrado(id, input);
    }

    @MutationMapping
    public TimbradoOutput desactivarTimbrado(@Argument Long id) {
        return timbradoService.desactivar(id);
    }

    @MutationMapping
    public TimbradoOutput activarTimbrado(@Argument Long id) {
        return timbradoService.activar(id);
    }

    @MutationMapping
    public Boolean eliminarTimbrado(@Argument Long id) {
        timbradoService.eliminarPorId(id);
        return true;
    }

    // ========================================================================
    // MUTATIONS - FACTURAS
    // ========================================================================

    @MutationMapping
    public FacturaOutput emitirFactura(@Argument FacturaInput input) {
        return facturaService.emitirFactura(input);
    }

    @MutationMapping
    public FacturaOutput anularFactura(@Argument Long id, @Argument String motivo) {
        return facturaService.anularFactura(id, motivo);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> buildPageResponse(Page<FacturaOutput> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("currentPage", page.getNumber());
        pageInfo.put("pageSize", page.getSize());
        pageInfo.put("hasNext", page.hasNext());
        pageInfo.put("hasPrevious", page.hasPrevious());
        
        response.put("pageInfo", pageInfo);
        
        return response;
    }
}
