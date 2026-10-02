package com.dev.ultron.dto.financiero.input;

import java.io.Serializable;
import java.util.List;

/**
 * Input para registrar o actualizar un timbrado.
 */
public record TimbradoInput(
        String numeroTimbrado,
        String establecimiento,
        String puntoExpedicion,
        Integer numeroInicial,
        Integer numeroFinal,
        Integer numeroActual,
        String fechaInicioVigencia,
        String fechaFinVigencia,
        Long idEmpresa,
        String tipoFactura,
        Boolean activo
) implements Serializable {
}
