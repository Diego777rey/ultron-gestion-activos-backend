package com.dev.ultron.dto.impresion.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpresionResultado implements Serializable {
    private Boolean success;
    private String message;

    public static ImpresionResultado ok(String message) {
        return ImpresionResultado.builder().success(true).message(message).build();
    }

    public static ImpresionResultado error(String message) {
        return ImpresionResultado.builder().success(false).message(message).build();
    }
}
