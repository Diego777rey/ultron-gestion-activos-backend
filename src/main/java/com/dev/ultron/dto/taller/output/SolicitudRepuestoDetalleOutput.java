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
public class SolicitudRepuestoDetalleOutput implements Serializable {
    private Long id_detalle;
    private Long id_producto;
    private String nombre_producto;
    private String codigo_producto;
    private BigDecimal cantidad;
}
