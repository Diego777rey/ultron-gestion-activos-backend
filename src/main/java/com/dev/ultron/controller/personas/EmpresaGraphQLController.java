package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.input.EmpresaInput;
import com.dev.ultron.dto.personas.output.EmpresaOutput;
import com.dev.ultron.service.personas.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controlador GraphQL para operaciones de empresas.
 */
@Controller
@RequiredArgsConstructor
public class EmpresaGraphQLController {

    private final EmpresaService empresaService;

    @QueryMapping
    public List<EmpresaOutput> empresas() {
        return empresaService.listarTodasEmpresas();
    }

    @QueryMapping
    public EmpresaOutput empresa(@Argument Long id) {
        return empresaService.obtenerEmpresaPorId(id);
    }

    @MutationMapping
    public EmpresaOutput registrarEmpresa(@Argument EmpresaInput input) {
        return empresaService.registrarEmpresa(input);
    }

    @MutationMapping
    public EmpresaOutput actualizarEmpresa(@Argument Long id, @Argument EmpresaInput input) {
        return empresaService.actualizarEmpresa(id, input);
    }

    @MutationMapping
    public Boolean eliminarEmpresa(@Argument Long id) {
        empresaService.eliminarPorId(id);
        return true;
    }
}
