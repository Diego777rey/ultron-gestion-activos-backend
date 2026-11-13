package com.dev.ultron.dto.inventario.output;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductoCatalogoWebOutput {
    private Long id_producto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioVenta;
    private String imagen;
    private String tipoIva;
    private CategoriaProductoOutput categoriaProducto;
    private List<PresentacionProductoOutput> presentaciones;
}
