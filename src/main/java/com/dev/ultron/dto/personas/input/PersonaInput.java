package com.dev.ultron.dto.personas.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaInput implements Serializable {
    private String nombre;
    private String apellido;
    private String documento;
    private String email;
    private String telefono;
    private String estado;
}
