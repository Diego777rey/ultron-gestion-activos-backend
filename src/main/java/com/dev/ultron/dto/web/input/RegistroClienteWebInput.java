package com.dev.ultron.dto.web.input;

import lombok.Data;

@Data
public class RegistroClienteWebInput {
    private String nombre;
    private String apellido;
    private String documento;
    private String ruc;
    private String email;
    private String telefono;
    private String direccion;
    private String password;
    private String passwordConfirmacion;
}
