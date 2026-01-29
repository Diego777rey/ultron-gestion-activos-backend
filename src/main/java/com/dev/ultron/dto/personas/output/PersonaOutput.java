package com.dev.ultron.dto.personas.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaOutput implements Serializable {
    private Long id_persona;
    private String nombre;
    private String apellido;
    private String documento;
    private String email;
    private String telefono;
    private String estado;
}
