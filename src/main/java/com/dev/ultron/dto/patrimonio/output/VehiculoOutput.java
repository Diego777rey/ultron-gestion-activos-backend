package com.dev.ultron.dto.patrimonio.output;

import com.dev.ultron.dto.personas.output.ClienteOutput;

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
    private ClienteOutput cliente;
    private String marca;
    private String modelo;
    private Integer anio;
    private String chapa;
    private String tipo_vehiculo;
}
