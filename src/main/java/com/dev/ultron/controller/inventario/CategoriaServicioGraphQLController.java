package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.CategoriaServicioInput;
import com.dev.ultron.dto.inventario.output.CategoriaServicioOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.inventario.CategoriaServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoriaServicioGraphQLController {

    private final CategoriaServicioService service;

    @QueryMapping
    public List<CategoriaServicioOutput> listarCategoriasServicio() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<CategoriaServicioOutput> listarCategoriasServicioPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public CategoriaServicioOutput buscarCategoriaServicioPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public List<CategoriaServicioOutput> listarCategoriasServicioRaiz() {
        return service.findRaices();
    }

    @QueryMapping
    public List<CategoriaServicioOutput> listarSubcategoriasServicio(@Argument Long idCategoriaPadre) {
        return service.findSubcategorias(idCategoriaPadre);
    }

    @MutationMapping
    public CategoriaServicioOutput registrarCategoriaServicio(@Argument CategoriaServicioInput input) {
        return service.save(input);
    }

    @MutationMapping
    public CategoriaServicioOutput actualizarCategoriaServicio(@Argument Long id, @Argument CategoriaServicioInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarCategoriaServicio(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
