package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.input.ClienteInput;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.service.personas.ClienteService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller GraphQL para el módulo de Clientes.
 * Expone las operaciones CRUD a través de queries y mutations.
 */
@Controller
public class ClienteGraphQLController {

    private final ClienteService clienteService;

    public ClienteGraphQLController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ==================== QUERIES ====================

    @QueryMapping
    public List<ClienteOutput> listarClientes() {
        return clienteService.listarTodosClientes();
    }

    @QueryMapping
    public com.dev.ultron.generic.PageResponse<ClienteOutput> listarClientesPaginado(@Argument int page, @Argument int size) {
        return clienteService.listarClientesPaginado(page, size);
    }

    @QueryMapping
    public ClienteOutput buscarClientePorId(@Argument Long id) {
        return clienteService.buscarClientePorId(id);
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public ClienteOutput registrarCliente(@Argument ClienteInput input) {
        return clienteService.registrarCliente(input);
    }

    @MutationMapping
    public ClienteOutput actualizarCliente(@Argument Long id,
                                           @Argument ClienteInput input) {
        return clienteService.actualizarCliente(id, input);
    }

    @MutationMapping
    public Boolean eliminarCliente(@Argument Long id) {
        return clienteService.eliminarCliente(id);
    }
}
