package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.AbrirCajaInput;
import com.dev.ultron.dto.financiero.input.CerrarCajaInput;
import com.dev.ultron.dto.financiero.output.ArqueoMonedaOutput;
import com.dev.ultron.dto.financiero.output.SesionCajaOutput;
import com.dev.ultron.dto.financiero.output.TicketCierreCajaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.ArqueoCajaService;
import com.dev.ultron.service.financiero.SesionCajaService;
import com.dev.ultron.service.financiero.TicketCierreCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SesionCajaGraphQLController {

    private final SesionCajaService service;
    private final TicketCierreCajaService ticketCierreCajaService;
    private final ArqueoCajaService arqueoCajaService;

    @QueryMapping
    public SesionCajaOutput sesionCajaAbierta(@Argument Long idCaja) {
        return service.sesionAbierta(idCaja);
    }

    @QueryMapping
    public TicketCierreCajaOutput ticketCierreCaja(@Argument Long idSesionCaja) {
        return ticketCierreCajaService.generar(idSesionCaja);
    }

    @QueryMapping
    public List<ArqueoMonedaOutput> arqueoSesionCaja(@Argument Long idSesionCaja) {
        return arqueoCajaService.calcular(idSesionCaja);
    }

    @QueryMapping
    public SesionCajaOutput buscarSesionCajaPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public PageResponse<SesionCajaOutput> listarSesionesCajaPaginado(
            @Argument int page,
            @Argument int size,
            @Argument String filter,
            @Argument Long idCaja,
            @Argument String estado,
            @Argument String fechaDesde,
            @Argument String fechaHasta) {
        return service.findAllPaginated(page, size, filter, idCaja, estado, fechaDesde, fechaHasta);
    }

    @QueryMapping
    public PageResponse<SesionCajaOutput> listarMisSesionesCajaCerradas(
            @Argument int page,
            @Argument int size,
            @Argument String fechaDesde,
            @Argument String fechaHasta) {
        return service.misSesionesCerradas(page, size, fechaDesde, fechaHasta);
    }

    @MutationMapping
    public SesionCajaOutput abrirCaja(@Argument AbrirCajaInput input) {
        return service.abrirCaja(input);
    }

    @MutationMapping
    public SesionCajaOutput cerrarCaja(@Argument CerrarCajaInput input) {
        return service.cerrarCaja(input);
    }
}
