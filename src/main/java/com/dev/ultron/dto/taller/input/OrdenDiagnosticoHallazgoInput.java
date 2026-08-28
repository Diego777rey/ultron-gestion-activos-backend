package com.dev.ultron.dto.taller.input;

import java.io.Serializable;

public record OrdenDiagnosticoHallazgoInput(
        String tipo,
        String gravedad,
        String sistema,
        String descripcion
) implements Serializable {
}
