package com.dev.ultron.controller.compras;

import com.dev.ultron.dto.compras.input.ProveedorInput;
import com.dev.ultron.dto.compras.output.ProveedorOutput;
import com.dev.ultron.service.compras.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controlador GraphQL para operaciones de proveedores.
 */
@Controller
@RequiredArgsConstructor
public class ProveedorGraphQLController {

    private final ProveedorService proveedorService;

    @QueryMapping
    public List<ProveedorOutput> proveedores() {
        return proveedorService.listarTodosProveedores();
    }

    @QueryMapping
    public List<ProveedorOutput> proveedoresActivos() {
        return proveedorService.listarProveedoresActivos();
    }

    @QueryMapping
    public ProveedorOutput proveedor(@Argument Long id) {
        return proveedorService.obtenerProveedorPorId(id);
    }

    @QueryMapping
    public ProveedorOutput proveedorPorRuc(@Argument String ruc) {
        return proveedorService.buscarPorRuc(ruc);
    }

    @MutationMapping
    public ProveedorOutput registrarProveedor(@Argument ProveedorInput input) {
        return proveedorService.registrarProveedor(input);
    }

    @MutationMapping
    public ProveedorOutput actualizarProveedor(@Argument Long id, @Argument ProveedorInput input) {
        return proveedorService.actualizarProveedor(id, input);
    }

    @MutationMapping
    public Boolean eliminarProveedor(@Argument Long id) {
        proveedorService.eliminarPorId(id);
        return true;
    }
}
