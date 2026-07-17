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
public class CerrarCajaInput implements Serializable {
    private Long idSesionCaja;
    private List<ConteoDenominacionInput> conteos;
}
