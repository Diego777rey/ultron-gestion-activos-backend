package com.dev.ultron.dto.patrimonio.output;

import com.dev.ultron.dto.personas.output.ClienteOutput;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipoOutput implements Serializable {
    private Long id_equipo;
    private ClienteOutput cliente;
    private VehiculoOutput vehiculo;
    private String tipo_equipo;
    private String marca;
    private String modelo;
    private String numero_serie;
    private String descripcion;
    private String estado;
    private String fecha_registro;
}
