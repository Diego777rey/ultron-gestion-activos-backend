package com.dev.ultron.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermisoDto {
    private Long id;
    private String modulo;
    private String accion;
    private String descripcion;
}
