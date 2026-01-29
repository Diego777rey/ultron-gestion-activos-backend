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
public class EmpresaOutput implements Serializable {
    private Long id_empresa;
    private String razon_social;
    private String ruc;
    private String direccion;
    private LocalDate fecha_creacion;
}
