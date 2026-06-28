package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.input.FuncionarioInput;
import com.dev.ultron.dto.personas.output.FuncionarioOutput;
import com.dev.ultron.service.personas.FuncionarioService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller GraphQL para el módulo de Funcionarios.
 * Expone las operaciones CRUD a través de queries y mutations.
 */
@Controller
public class FuncionarioGraphQLController {

    private final FuncionarioService funcionarioService;

    public FuncionarioGraphQLController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    // ==================== QUERIES ====================

    @QueryMapping
    public List<FuncionarioOutput> listarFuncionarios() {
        return funcionarioService.listarTodosFuncionarios();
    }

    @QueryMapping
    public FuncionarioOutput buscarFuncionarioPorId(@Argument Long id) {
        return funcionarioService.buscarFuncionarioPorId(id);
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public FuncionarioOutput registrarFuncionario(@Argument FuncionarioInput input) {
        return funcionarioService.registrarFuncionario(input);
    }

    @MutationMapping
    public FuncionarioOutput actualizarFuncionario(@Argument Long id,
                                                    @Argument FuncionarioInput input) {
        return funcionarioService.actualizarFuncionario(id, input);
    }

    @MutationMapping
    public Boolean eliminarFuncionario(@Argument Long id) {
        return funcionarioService.eliminarFuncionario(id);
    }
}
