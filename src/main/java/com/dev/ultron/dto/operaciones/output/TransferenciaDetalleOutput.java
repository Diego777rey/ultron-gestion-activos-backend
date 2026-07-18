package com.dev.ultron.dto.operaciones.output;

import com.dev.ultron.dto.inventario.output.ProductoOutput;
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
public class TransferenciaDetalleOutput implements Serializable {
    private Long id_detalle;
    private ProductoOutput producto;
    private BigDecimal cantidad;
}
