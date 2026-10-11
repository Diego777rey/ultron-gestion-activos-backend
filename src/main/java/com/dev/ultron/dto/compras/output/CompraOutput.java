package com.dev.ultron.dto.compras.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraOutput {
    private Long id_compra;
    private Long id_proveedor;
    private String proveedor_nombre;
    private Long id_vendedor;
    private String vendedor_nombre;
    private String numero_factura;
    private String timbrado;
    private String fecha_factura;
    private String fecha_compra;
    private String forma_pago;
    private String moneda;
    private BigDecimal cotizacion;
    private BigDecimal subtotal;
    private BigDecimal total_iva;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private String fecha_registro;
    private String fecha_ultima_modificacion;
    private List<ItemCompraOutput> items;
}
