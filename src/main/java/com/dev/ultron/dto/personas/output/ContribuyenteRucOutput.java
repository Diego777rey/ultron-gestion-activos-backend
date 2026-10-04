package com.dev.ultron.dto.personas.output;

import lombok.Builder;

/**
 * Datos públicos de un contribuyente según la DNIT.
 *
 * @param ruc            RUC con dígito verificador (ej. 80012345-0).
 * @param documento      Número de documento sin dígito verificador.
 * @param razonSocial    Razón social tal como figura en la DNIT.
 * @param nombre         Nombre para registrar: en personas físicas, "NOMBRE APELLIDO".
 * @param estado         ACTIVO, SUSPENDIDO, CANCELADO, BLOQUEADO, etc.
 */
@Builder
public record ContribuyenteRucOutput(
        String ruc,
        String documento,
        Integer dv,
        String razonSocial,
        String nombre,
        String estado,
        boolean activo,
        boolean personaJuridica,
        boolean entidadPublica) {
}
