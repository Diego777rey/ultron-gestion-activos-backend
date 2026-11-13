package com.dev.ultron.dto.web.input;

import java.util.List;

import lombok.Data;

@Data
public class PedidoWebInput {
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String direccionEnvio;
    private String ciudad;
    private String notas;
    private String formaPago;
    private PagoTarjetaInput pagoTarjeta;
    private List<DetallePedidoWebInput> detalles;
}
