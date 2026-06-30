package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.input.UsuarioInput;
import com.dev.ultron.dto.personas.output.UsuarioOutput;
import com.dev.ultron.service.personas.UsuarioService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UsuarioGraphQLController {

    private final UsuarioService usuarioService;

    public UsuarioGraphQLController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @QueryMapping
    public List<UsuarioOutput> listarUsuarios() {
        return usuarioService.listarTodosUsuarios();
    }

    @QueryMapping
    public com.dev.ultron.generic.PageResponse<UsuarioOutput> listarUsuariosPaginado(@Argument int page, @Argument int size) {
        return usuarioService.listarUsuariosPaginado(page, size);
    }

    @QueryMapping
    public UsuarioOutput buscarUsuarioPorId(@Argument Long id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @MutationMapping
    public UsuarioOutput registrarUsuario(@Argument UsuarioInput input) {
        return usuarioService.registrarUsuario(input);
    }

    @MutationMapping
    public UsuarioOutput actualizarUsuario(@Argument Long id, @Argument UsuarioInput input) {
        return usuarioService.actualizarUsuario(id, input);
    }

    @MutationMapping
    public Boolean eliminarUsuario(@Argument Long id) {
        return usuarioService.eliminarUsuario(id);
    }
}
