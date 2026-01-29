package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoOutput implements Serializable {
    private Long id_ingreso;
    private Long id_movimiento;
    private String descripcion;
    private String origen;
    private String cliente_o_fuente;
    private String observaciones;
}
