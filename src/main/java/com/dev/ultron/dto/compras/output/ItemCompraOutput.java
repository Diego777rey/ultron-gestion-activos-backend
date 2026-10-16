package com.dev.ultron.dto.compras.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCompraOutput {
    private Long id_item_compra;
    private Long id_compra;
    private Long id_producto;
    private String producto_nombre;
    private String producto_codigo;
    private BigDecimal cantidad;
    private BigDecimal precio_costo;
    private BigDecimal subtotal;
    private String tipo_iva;
    private BigDecimal iva;
    private String fecha_vencimiento;
    private String lote;
    private String observaciones;
    private String fecha_registro;
}
