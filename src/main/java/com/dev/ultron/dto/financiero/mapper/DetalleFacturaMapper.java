package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.DetalleFactura;
import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.dto.financiero.input.DetalleFacturaInput;
import com.dev.ultron.dto.financiero.output.DetalleFacturaOutput;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DetalleFacturaMapper {

    @Mapping(target = "id_detalle_factura", ignore = true)
    @Mapping(target = "factura", source = "factura")
    @Mapping(target = "producto", source = "producto")
    @Mapping(target = "servicio", source = "servicio")
    @Mapping(target = "presentacion", source = "presentacion")
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "codigo", source = "input.codigo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cantidad", source = "input.cantidad")
    @Mapping(target = "precio_unitario", source = "input.precioUnitario")
    @Mapping(target = "tipo_iva", source = "input.tipoIva", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "monto_iva", ignore = true)
    @Mapping(target = "total_linea", ignore = true)
    @Mapping(target = "numero_linea", source = "numeroLinea")
    DetalleFactura toEntity(
            DetalleFacturaInput input,
            Factura factura,
            Producto producto,
            Servicio servicio,
            PresentacionProducto presentacion,
            Integer numeroLinea
    );

    @Mapping(target = "id_producto", source = "producto.id_producto")
    @Mapping(target = "id_servicio", source = "servicio.id_servicio")
    @Mapping(target = "id_presentacion", source = "presentacion.id_presentacion_producto")
    DetalleFacturaOutput toOutput(DetalleFactura detalle);
}
