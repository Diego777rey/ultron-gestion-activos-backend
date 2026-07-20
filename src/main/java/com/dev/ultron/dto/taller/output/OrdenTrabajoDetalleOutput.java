package com.dev.ultron.dto.taller.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoDetalleOutput implements Serializable {
    private Long id_detalle;
    private String tipo;
    private Long id_producto;
    private String nombre_producto;
    private Long id_servicio;
    private String nombre_servicio;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precio_unitario;
    private BigDecimal subtotal;
    private String etapa_origen;
}
