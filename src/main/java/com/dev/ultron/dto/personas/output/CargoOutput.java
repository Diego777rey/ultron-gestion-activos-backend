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
public class CargoOutput implements Serializable {
    private Long id_cargo;
    private String nombre;
    private String descripcion;
    private Integer nivel_autorizacion;
}
