package com.dev.ultron.dto.personas.input;

import java.io.Serializable;
import java.util.List;

public record RoleInput(
        String descripcion,
        String activo) implements Serializable {
}
