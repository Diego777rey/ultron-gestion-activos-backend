package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductoOutput {
    private Long id_producto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private String ubicacion;
    private Boolean estado;
    private CategoriaProductoOutput categoriaProducto;
}
