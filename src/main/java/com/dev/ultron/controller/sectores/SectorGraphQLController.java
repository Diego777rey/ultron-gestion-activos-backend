package com.dev.ultron.controller.sectores;

import com.dev.ultron.dto.sectores.input.SectorInput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.sectores.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SectorGraphQLController {

    private final SectorService service;

    @QueryMapping
    public List<SectorOutput> listarSectores() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<SectorOutput> listarSectoresPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public SectorOutput buscarSectorPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public SectorOutput registrarSector(@Argument SectorInput input) {
        return service.save(input);
    }

    @MutationMapping
    public SectorOutput actualizarSector(@Argument Long id, @Argument SectorInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarSector(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
