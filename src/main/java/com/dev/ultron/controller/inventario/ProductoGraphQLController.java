package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.ProductoInput;
import com.dev.ultron.dto.inventario.output.ProductoOutput;
import com.dev.ultron.generic.PageResponse;
import com.dev.ultron.service.inventario.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductoGraphQLController {

    private final ProductoService service;

    @QueryMapping
    public List<ProductoOutput> listarProductos() {
        return service.findAll();
    }

    @QueryMapping
    public PageResponse<ProductoOutput> listarProductosPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return service.findAllPaginated(page, size, filter);
    }

    @QueryMapping
    public ProductoOutput buscarProductoPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public ProductoOutput registrarProducto(@Argument ProductoInput input) {
        return service.save(input);
    }

    @MutationMapping
    public ProductoOutput actualizarProducto(@Argument Long id, @Argument ProductoInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarProducto(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
