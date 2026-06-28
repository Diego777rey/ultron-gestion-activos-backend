package com.dev.ultron.dto.personas.input;

import java.io.Serializable;

public record PersonaInput(
        String nombre,
        String apellido,
        String documento,
        String email,
        String telefono,
        String direccion,
        String estado) implements Serializable {
}
