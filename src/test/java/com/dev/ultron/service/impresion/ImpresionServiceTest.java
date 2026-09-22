package com.dev.ultron.service.impresion;

import com.dev.ultron.dto.impresion.output.ImpresionResultado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ImpresionServiceTest {

    @Test
    void imprimirPruebaSinNombreFalla() {
        ImpresionService service = new ImpresionService();
        ImpresionResultado resultado = service.imprimirPrueba("  ");
        assertFalse(resultado.getSuccess());
        assertEquals("Indicá el nombre de la impresora térmica", resultado.getMessage());
    }
}
