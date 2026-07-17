package com.dev.ultron.controller.sectores;

import com.dev.ultron.dto.sectores.input.ZonaInput;
import com.dev.ultron.dto.sectores.output.ZonaOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.sectores.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ZonaGraphQLController {

    private final ZonaService service;

    @QueryMapping
    public List<ZonaOutput> listarZonas() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<ZonaOutput> listarZonasPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public ZonaOutput buscarZonaPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public List<ZonaOutput> listarZonasPorSector(@Argument Long idSector) {
        return service.findBySector(idSector);
    }

    @MutationMapping
    public ZonaOutput registrarZona(@Argument ZonaInput input) {
        return service.save(input);
    }

    @MutationMapping
    public ZonaOutput actualizarZona(@Argument Long id, @Argument ZonaInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarZona(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
