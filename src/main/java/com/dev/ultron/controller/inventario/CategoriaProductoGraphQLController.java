package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.CategoriaProductoInput;
import com.dev.ultron.dto.inventario.output.CategoriaProductoOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.inventario.CategoriaProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoriaProductoGraphQLController {

    private final CategoriaProductoService service;

    @QueryMapping
    public List<CategoriaProductoOutput> listarCategoriasProducto() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<CategoriaProductoOutput> listarCategoriasProductoPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public CategoriaProductoOutput buscarCategoriaProductoPorId(@Argument Long id) {
        return service.findById(id);
    }

    @QueryMapping
    public List<CategoriaProductoOutput> listarCategoriasProductoRaiz() {
        return service.findRaices();
    }

    @QueryMapping
    public List<CategoriaProductoOutput> listarSubcategoriasProducto(@Argument Long idCategoriaPadre) {
        return service.findSubcategorias(idCategoriaPadre);
    }

    @MutationMapping
    public CategoriaProductoOutput registrarCategoriaProducto(@Argument CategoriaProductoInput input) {
        return service.save(input);
    }

    @MutationMapping
    public CategoriaProductoOutput actualizarCategoriaProducto(@Argument Long id, @Argument CategoriaProductoInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarCategoriaProducto(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
