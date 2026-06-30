package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Role;
import com.dev.ultron.dto.personas.input.RoleInput;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.utilitarios.StringUtil;

public class RoleMapper {

    private RoleMapper() {
    }

    public static Role toEntity(RoleInput input) {
        if (input == null) return null;
        return Role.builder()
                .descripcion(StringUtil.toUpperCase(input.descripcion()))
                .activo(input.activo() != null ? input.activo() : "ACTIVO")
                .build();
    }

    public static void updateEntity(Role role, RoleInput input) {
        if (input == null || role == null) return;
        role.setDescripcion(StringUtil.toUpperCase(input.descripcion()));
        if (input.activo() != null) {
            role.setActivo(input.activo());
        }
    }

    public static RoleOutput toOutput(Role role) {
        if (role == null) return null;
        return RoleOutput.builder()
                .id(role.getId())
                .descripcion(role.getDescripcion())
                .activo(role.getActivo())
                .build();
    }
}
