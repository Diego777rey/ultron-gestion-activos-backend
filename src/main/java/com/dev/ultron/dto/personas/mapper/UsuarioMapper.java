package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Role;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.personas.UsuarioRole;
import com.dev.ultron.dto.personas.input.UsuarioInput;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.dto.personas.output.UsuarioOutput;
import com.dev.ultron.utilitarios.StringUtil;

import java.util.Collections;
import java.util.List;

public class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toEntity(UsuarioInput input, String encodedPassword) {
        if (input == null) return null;
        return Usuario.builder()
                .username(StringUtil.toUpperCase(input.username()))
                .password(encodedPassword)
                .email(input.email())
                .activo(input.activo() != null ? input.activo() : true)
                .build();
    }

    public static void updateEntity(Usuario usuario, UsuarioInput input) {
        if (input == null || usuario == null) return;
        usuario.setUsername(StringUtil.toUpperCase(input.username()));
        usuario.setEmail(input.email());
        if (input.activo() != null) {
            usuario.setActivo(input.activo());
        }
    }

    public static UsuarioOutput toOutput(Usuario usuario) {
        if (usuario == null) return null;
        return UsuarioOutput.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .id_funcionario(usuario.getFuncionario() != null
                        ? usuario.getFuncionario().getId_funcionario()
                        : null)
                .funcionario(FuncionarioMapper.toOutput(usuario.getFuncionario()))
                .roles(mapRoles(usuario))
                .build();
    }

    private static List<RoleOutput> mapRoles(Usuario usuario) {
        if (usuario.getUsuarioRoles() == null || usuario.getUsuarioRoles().isEmpty()) {
            return Collections.emptyList();
        }
        return usuario.getUsuarioRoles().stream()
                .map(UsuarioRole::getRole)
                .map(RoleMapper::toOutput)
                .toList();
    }
}
