package com.dev.ultron.dto.personas.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCargoOutput implements Serializable {
    private Long id_asignacion;
    private Long id_persona;
    private Long id_cargo;
    private Long id_empresa;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private Boolean activo;
}
