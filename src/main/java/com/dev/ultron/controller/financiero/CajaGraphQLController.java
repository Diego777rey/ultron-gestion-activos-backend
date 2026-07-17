package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.CajaInput;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CajaGraphQLController {

    private final CajaService service;

    @QueryMapping
    public List<CajaOutput> listarCajas() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<CajaOutput> listarCajasPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public CajaOutput buscarCajaPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public CajaOutput registrarCaja(@Argument CajaInput input) {
        return service.save(input);
    }

    @MutationMapping
    public CajaOutput actualizarCaja(@Argument Long id, @Argument CajaInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarCaja(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
