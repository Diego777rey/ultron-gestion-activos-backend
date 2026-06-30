package com.dev.ultron.dto.personas.input;

import java.io.Serializable;
import java.util.List;

public record UsuarioInput(
        String username,
        String password,
        String email,
        Boolean activo,
        Long id_funcionario,
        List<Long> roleIds) implements Serializable {
}
