package com.dev.ultron.dto.impresion.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketVentaInput implements Serializable {
    private String titulo;
    private String subtitulo;
    private String numero;
    private String fecha;
    private String cajero;
    private String cliente;
    private List<TicketLineaInput> lineas;
    private BigDecimal descuento;
    private BigDecimal total;
    private String pie;
}
