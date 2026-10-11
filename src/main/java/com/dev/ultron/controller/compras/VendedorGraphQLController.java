package com.dev.ultron.controller.compras;

import com.dev.ultron.dto.compras.input.VendedorInput;
import com.dev.ultron.dto.compras.output.VendedorOutput;
import com.dev.ultron.service.compras.VendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controlador GraphQL para operaciones de vendedores.
 */
@Controller
@RequiredArgsConstructor
public class VendedorGraphQLController {

    private final VendedorService vendedorService;

    @QueryMapping
    public List<VendedorOutput> vendedores() {
        return vendedorService.listarTodosVendedores();
    }

    @QueryMapping
    public List<VendedorOutput> vendedoresActivos() {
        return vendedorService.listarVendedoresActivos();
    }

    @QueryMapping
    public List<VendedorOutput> vendedoresPorProveedor(@Argument Long idProveedor) {
        return vendedorService.listarVendedoresPorProveedor(idProveedor);
    }

    @QueryMapping
    public VendedorOutput vendedor(@Argument Long id) {
        return vendedorService.obtenerVendedorPorId(id);
    }

    @MutationMapping
    public VendedorOutput registrarVendedor(@Argument VendedorInput input) {
        return vendedorService.registrarVendedor(input);
    }

    @MutationMapping
    public VendedorOutput actualizarVendedor(@Argument Long id, @Argument VendedorInput input) {
        return vendedorService.actualizarVendedor(id, input);
    }

    @MutationMapping
    public Boolean eliminarVendedor(@Argument Long id) {
        vendedorService.eliminarPorId(id);
        return true;
    }
}
