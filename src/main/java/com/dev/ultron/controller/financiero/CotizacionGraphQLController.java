package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.CotizacionInput;
import com.dev.ultron.dto.financiero.output.CotizacionOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.CotizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CotizacionGraphQLController {

    private final CotizacionService service;

    @QueryMapping
    public List<CotizacionOutput> listarCotizaciones() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<CotizacionOutput> listarCotizacionesPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public CotizacionOutput obtenerCotizacionPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public CotizacionOutput crearCotizacion(@Argument CotizacionInput input) {
        return service.save(input);
    }

    @MutationMapping
    public CotizacionOutput actualizarCotizacion(@Argument Long id, @Argument CotizacionInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarCotizacion(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
