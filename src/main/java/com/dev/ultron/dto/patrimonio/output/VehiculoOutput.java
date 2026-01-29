package com.dev.ultron.dto.patrimonio.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VehiculoOutput extends BienOutput {
    private String marca;
    private String modelo;
    private Integer anio;
    private String chapa;
    private String tipo_vehiculo;
}
