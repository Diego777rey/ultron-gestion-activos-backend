package com.dev.ultron.dto.patrimonio.input;

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
public class VehiculoInput extends BienInput {
    private String marca;
    private String modelo;
    private Integer anio;
    private String chapa;
    private String tipo_vehiculo;
}
