package com.dev.ultron.dto.web.output;

import lombok.Data;

@Data
public class SesionClienteWebOutput {
    private String token;
    private Long idCliente;
    private Long idCuenta;
    private String email;
    private String nombre;
    private String apellido;
    private String documento;
    private String ruc;
    private String telefono;
    private String direccion;
    private String mensaje;
}
