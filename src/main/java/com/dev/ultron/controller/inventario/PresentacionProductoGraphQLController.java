package com.dev.ultron.controller.inventario;

import com.dev.ultron.dto.inventario.input.PresentacionProductoInput;
import com.dev.ultron.dto.inventario.output.PresentacionProductoOutput;
import com.dev.ultron.service.inventario.PresentacionProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PresentacionProductoGraphQLController {

    private final PresentacionProductoService service;

    @QueryMapping
    public List<PresentacionProductoOutput> listarPresentacionesPorProducto(@Argument Long idProducto) {
        return service.findByProducto(idProducto);
    }

    @QueryMapping
    public PresentacionProductoOutput buscarPresentacionProductoPorId(@Argument Long id) {
        return service.findById(id);
    }

    @MutationMapping
    public PresentacionProductoOutput registrarPresentacionProducto(@Argument PresentacionProductoInput input) {
        return service.save(input);
    }

    @MutationMapping
    public PresentacionProductoOutput actualizarPresentacionProducto(@Argument Long id, @Argument PresentacionProductoInput input) {
        return service.update(id, input);
    }

    @MutationMapping
    public Boolean eliminarPresentacionProducto(@Argument Long id) {
        service.eliminarPorId(id);
        return true;
    }
}
