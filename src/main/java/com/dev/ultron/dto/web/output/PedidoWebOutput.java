package com.dev.ultron.dto.web.output;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class PedidoWebOutput {
    private Long id_pedido_web;
    private String codigo;
    private String estado;
    private String formaPago;
    private String moneda;
    private BigDecimal total;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String direccionEnvio;
    private String ciudad;
    private String notas;
    private String pagoTransaccionId;
    private String pagoUltimosCuatro;
    private String pagoMensaje;
    private String creadoEn;
    private List<DetallePedidoWebOutput> detalles;
}
