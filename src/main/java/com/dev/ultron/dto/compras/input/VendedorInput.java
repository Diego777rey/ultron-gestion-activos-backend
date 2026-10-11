package com.dev.ultron.dto.compras.input;

import java.io.Serializable;

/**
 * Input para registrar o actualizar un vendedor.
 */
public record VendedorInput(
        Long idProveedor,
        String nombre,
        String apellido,
        String telefono,
        String email,
        String celular,
        String cargo,
        String observaciones,
        Boolean activo
) implements Serializable {
}
