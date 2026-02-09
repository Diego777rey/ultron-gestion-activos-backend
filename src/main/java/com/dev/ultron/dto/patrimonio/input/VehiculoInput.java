package com.dev.ultron.dto.patrimonio.input;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VehiculoInput(
        String tipo,
        String descripcion,
        BigDecimal valor,
        LocalDate fecha_adquisicion,
        Long id_empresa,
        String estado,
        String marca,
        String modelo,
        Integer anio,
        String chapa,
        String tipo_vehiculo) implements Serializable {
}
