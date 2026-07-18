package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.PresentacionInput;
import com.dev.ultron.dto.inventario.output.PresentacionOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.inventario.PresentacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PresentacionGraphQLController {

    private final PresentacionService service;

    @QueryMapping
    public List<PresentacionOutput> listarPresentaciones() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<PresentacionOutput> listarPresentacionesPaginado(
            @Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public PresentacionOutput buscarPresentacionPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public PresentacionOutput registrarPresentacion(@Argument PresentacionInput input) {
        return service.save(input);
    }

    @MutationMapping
    public PresentacionOutput actualizarPresentacion(@Argument Long id, @Argument PresentacionInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarPresentacion(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
