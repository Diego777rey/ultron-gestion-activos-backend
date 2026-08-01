package com.dev.ultron.dto.operaciones.input;

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
public class TransferenciaDetalleInput implements Serializable {
    private Long idProducto;
    private BigDecimal cantidad;
}
