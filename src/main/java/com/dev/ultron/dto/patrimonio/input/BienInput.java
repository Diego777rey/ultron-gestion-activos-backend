package com.dev.ultron.dto.patrimonio.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BienInput implements Serializable {
    private String tipo;
    private String descripcion;
    private BigDecimal valor;
    private LocalDate fecha_adquisicion;
    private Long id_empresa;
    private String estado;
}
