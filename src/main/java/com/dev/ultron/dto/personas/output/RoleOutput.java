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
public class RoleOutput implements Serializable {
    private Long id;
    private String descripcion;
    private String activo;
}
