package com.dev.ultron.dto.personas.input;

import com.dev.ultron.dto.patrimonio.input.BienInput;
import java.io.Serializable;
import java.math.BigDecimal;

public record BienConPorcentajeInput(
        BienInput bien,
        BigDecimal porcentaje) implements Serializable {
}
