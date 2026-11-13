package com.dev.ultron.dto.web.output;

import lombok.Data;

@Data
public class SuscripcionNewsletterOutput {
    private Long id_suscripcion;
    private String email;
    private String nombre;
    private Boolean activo;
    private String creadoEn;
    private String mensaje;
}
