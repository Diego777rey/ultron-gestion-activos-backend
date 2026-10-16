package com.dev.ultron.dto.compras.input;

import java.io.Serializable;

/**
 * Input para registrar o actualizar un proveedor.
 */
public record ProveedorInput(
        String tipoPersona,
        String nombre,
        String apellido,
        String razonSocial,
        String nombreComercial,
        String nombreFantasia,
        String ruc,
        String documento,
        String direccion,
        String telefono,
        String email,
        String ciudad,
        String pais,
        String codigoPostal,
        String sitioWeb,
        String observaciones,
        Boolean activo
) implements Serializable {
}
