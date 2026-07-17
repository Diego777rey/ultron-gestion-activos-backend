package com.dev.ultron.dto.financiero.input;

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
public class DetalleVentaInput implements Serializable {
    private Long idProducto;
    private Long idPresentacion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
}
