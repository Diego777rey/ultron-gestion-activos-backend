package com.dev.ultron.dto.patrimonio.input;

import java.io.Serializable;

public record EquipoInput(
        Long id_cliente,
        Long id_vehiculo,
        String tipo_equipo,
        String marca,
        String modelo,
        String numero_serie,
        String descripcion,
        String estado) implements Serializable {
}
