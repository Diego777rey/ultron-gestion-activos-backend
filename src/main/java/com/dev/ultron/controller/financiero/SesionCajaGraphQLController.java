package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.AbrirCajaInput;
import com.dev.ultron.dto.financiero.input.CerrarCajaInput;
import com.dev.ultron.dto.financiero.output.SesionCajaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.SesionCajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SesionCajaGraphQLController {

    private final SesionCajaService service;

    @QueryMapping
    public SesionCajaOutput sesionCajaAbierta(@Argument Long idCaja) {
        return service.sesionAbierta(idCaja);
    }

    @QueryMapping
    public SesionCajaOutput buscarSesionCajaPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public PageResponse<SesionCajaOutput> listarSesionesCajaPaginado(
            @Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
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
