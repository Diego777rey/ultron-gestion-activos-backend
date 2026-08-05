package com.dev.ultron.dto.taller.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenDiagnosticoOutput implements Serializable {
    private String fecha_inicio_estimada;
    private String fecha_fin_estimada;
    private Boolean presupuesto_aprobado;
    private BigDecimal total_presupuesto;
    private String observaciones;
}
