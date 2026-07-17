package com.dev.ultron.dto.financiero.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbrirCajaInput implements Serializable {
    private Long idCaja;
    private Long idMaletin;
    private Long idPersona;
    private List<ConteoDenominacionInput> conteos;
}
