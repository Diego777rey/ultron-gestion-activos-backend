package com.dev.ultron.dto.taller.input;

import java.io.Serializable;

public record OrdenRecepcionInput(
        String descripcion_falla
) implements Serializable {
}
