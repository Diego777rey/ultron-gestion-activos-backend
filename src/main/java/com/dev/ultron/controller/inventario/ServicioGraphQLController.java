package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.ServicioInput;
import com.dev.ultron.dto.inventario.output.ServicioOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.inventario.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServicioGraphQLController {

    private final ServicioService service;

    @QueryMapping
    public List<ServicioOutput> listarServicios() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<ServicioOutput> listarServiciosPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public ServicioOutput buscarServicioPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public ServicioOutput registrarServicio(@Argument ServicioInput input) {
        return service.save(input);
    }

    @MutationMapping
    public ServicioOutput actualizarServicio(@Argument Long id, @Argument ServicioInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarServicio(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
