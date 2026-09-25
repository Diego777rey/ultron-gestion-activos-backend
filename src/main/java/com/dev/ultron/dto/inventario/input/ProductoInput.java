package com.dev.ultron.dto.inventario.input;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductoInput {
    private String codigo;
    private String nombre;
    private String descripcion;
    private String codigoBarras;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private String ubicacion;
    private Boolean estado;
    private String imagen;
    private Long idCategoriaProducto;
    private List<PresentacionProductoInput> presentaciones;
}
