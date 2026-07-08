package com.dev.ultron.controller.personas;

import com.dev.ultron.dto.personas.input.RoleInput;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.service.personas.RoleService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RoleGraphQLController {

    private final RoleService roleService;

    public RoleGraphQLController(RoleService roleService) {
        this.roleService = roleService;
    }

    @QueryMapping
    public List<RoleOutput> listarRoles() {
        return roleService.listarTodosRoles();
    }

    @QueryMapping
    public com.dev.ultron.generic.PageResponse<RoleOutput> listarRolesPaginado(@Argument int page, @Argument int size, @Argument String filter) {
        return roleService.listarRolesPaginado(page, size, filter);
    }

    @QueryMapping
    public com.dev.ultron.generic.PageResponse<RoleOutput> rolesUsuarioPaginado(
            @Argument Long usuarioId, @Argument int page, @Argument int size, @Argument String filter) {
        return roleService.rolesUsuarioPaginado(usuarioId, page, size, filter);
    }

    @QueryMapping
    public com.dev.ultron.generic.PageResponse<RoleOutput> rolesDisponiblesUsuarioPaginado(
            @Argument Long usuarioId, @Argument int page, @Argument int size, @Argument String filter) {
        return roleService.rolesDisponiblesUsuarioPaginado(usuarioId, page, size, filter);
    }

    @QueryMapping
    public RoleOutput buscarRolePorId(@Argument Long id) {
        return roleService.buscarRolePorId(id);
    }

    @MutationMapping
    public RoleOutput registrarRole(@Argument RoleInput input) {
        return roleService.registrarRole(input);
    }

    @MutationMapping
    public RoleOutput actualizarRole(@Argument Long id, @Argument RoleInput input) {
        return roleService.actualizarRole(id, input);
    }

    @MutationMapping
    public Boolean eliminarRole(@Argument Long id) {
        return roleService.eliminarRole(id);
    }
}
