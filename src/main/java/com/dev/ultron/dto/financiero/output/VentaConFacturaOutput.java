package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaConFacturaOutput implements Serializable {
    private VentaOutput venta;
    private FacturaOutput factura;
}
