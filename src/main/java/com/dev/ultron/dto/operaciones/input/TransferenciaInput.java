package com.dev.ultron.dto.operaciones.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaInput implements Serializable {
    private Long idSectorOrigen;
    private Long idSectorDestino;
    private Long idPersona;
}
