package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductoInput {
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private String ubicacion;
    private Boolean estado;
    private Long idCategoriaProducto;
}
