package com.dev.ultron.dto.taller.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDiagnosticoHallazgoOutput implements Serializable {
    private Long id_hallazgo;
    private String tipo;
    private String gravedad;
    private String sistema;
    private String descripcion;
    private String etapa_origen;
}
