package com.dev.ultron.dto.personas.input;

import java.io.Serializable;
import java.util.List;

public record RegistroPersonaConBienesInput(
        PersonaInput persona,
        List<BienConPorcentajeInput> bienes) implements Serializable {
}
