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
public class ProductoProveedorOutput {
    private Long id_producto_proveedor;
    private Long id_producto;
    private String producto_nombre;
    private String producto_codigo;
    private Long id_proveedor;
    private String proveedor_nombre;
    private BigDecimal precio_ultima_compra;
    private String fecha_ultima_compra;
    private String observaciones;
    private Boolean activo;
    private String fecha_registro;
}
