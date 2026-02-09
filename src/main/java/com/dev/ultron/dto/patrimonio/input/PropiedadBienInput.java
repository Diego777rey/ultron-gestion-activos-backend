package com.dev.ultron.dto.patrimonio.input;

import java.io.Serializable;
import java.math.BigDecimal;

public record PropiedadBienInput(
        Long id_bien,
        Long id_persona,
        BigDecimal porcentaje_propiedad) implements Serializable {
}
