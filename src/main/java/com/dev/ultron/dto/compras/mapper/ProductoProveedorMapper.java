package com.dev.ultron.dto.compras.mapper;

import com.dev.ultron.domain.compras.ProductoProveedor;
import com.dev.ultron.dto.compras.output.ProductoProveedorOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoProveedorMapper {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "id_producto", source = "producto.id_producto")
    @Mapping(target = "producto_nombre", source = "producto.nombre")
    @Mapping(target = "producto_codigo", source = "producto.codigo")
    @Mapping(target = "id_proveedor", source = "proveedor.id_proveedor")
    @Mapping(target = "proveedor_nombre", expression = "java(getNombreProveedor(productoProveedor))")
    @Mapping(target = "fecha_ultima_compra", expression = "java(formatDateTime(productoProveedor.getFecha_ultima_compra()))")
    @Mapping(target = "fecha_registro", expression = "java(formatDateTime(productoProveedor.getFecha_registro()))")
    ProductoProveedorOutput toOutput(ProductoProveedor productoProveedor);

    List<ProductoProveedorOutput> toOutputList(List<ProductoProveedor> list);

    default String getNombreProveedor(ProductoProveedor pp) {
        if (pp.getProveedor() == null) {
            return null;
        }
        var proveedor = pp.getProveedor();
        if (proveedor.getTipo_persona() == com.dev.ultron.domain.compras.Proveedor.TipoPersona.FISICA) {
            return (proveedor.getNombre() + " " + proveedor.getApellido()).trim();
        } else {
            return proveedor.getRazon_social();
        }
    }

    default String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
}
