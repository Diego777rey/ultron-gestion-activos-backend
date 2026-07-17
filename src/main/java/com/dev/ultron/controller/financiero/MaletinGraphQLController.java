package com.dev.ultron.controller.financiero;

import com.dev.ultron.dto.financiero.input.MaletinInput;
import com.dev.ultron.dto.financiero.output.MaletinOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.financiero.MaletinService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MaletinGraphQLController {

    private final MaletinService service;

    @QueryMapping
    public List<MaletinOutput> listarMaletines() {
        return service.findAll();
    }

    @QueryMapping
    public List<MaletinOutput> listarMaletinesDisponibles() {
        return service.findDisponibles();
    }

    @QueryMapping
    public PageResponse<MaletinOutput> listarMaletinesPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public MaletinOutput buscarMaletinPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public MaletinOutput registrarMaletin(@Argument MaletinInput input) {
        return service.save(input);
    }

    @MutationMapping
    public MaletinOutput actualizarMaletin(@Argument Long id, @Argument MaletinInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarMaletin(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
