package com.dev.ultron.dto.personas.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadAccionariaInput implements Serializable {
    private Long id_persona;
    private Long id_empresa;
    private BigDecimal porcentaje_acciones;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
}
