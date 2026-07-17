package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaOutput implements Serializable {
    private Long id_venta;
    private String numero;
    private LocalDateTime fecha;
    private Long idSesionCaja;
    private Long idCliente;
    private String clienteNombre;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String estado;
    private List<DetalleVentaOutput> detalles;
}
