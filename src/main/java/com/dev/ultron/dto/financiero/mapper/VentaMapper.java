package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.DetalleVenta;
import com.dev.ultron.domain.financiero.Venta;
import com.dev.ultron.dto.financiero.output.DetalleVentaOutput;
import com.dev.ultron.dto.financiero.output.VentaOutput;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface VentaMapper {

    @Mapping(target = "idSesionCaja", source = "sesionCaja.id_sesion_caja")
    @Mapping(target = "idCliente", source = "cliente.id_cliente")
    @Mapping(target = "clienteNombre", expression = "java(resolveClienteNombre(entity))")
    VentaOutput toOutput(Venta entity);

    @Mapping(target = "idProducto", source = "producto.id_producto")
    @Mapping(target = "idOrdenTrabajo", source = "ordenTrabajo.id_orden_trabajo")
    @Mapping(target = "productoNombre", expression = "java(resolveDetalleNombre(entity))")
    DetalleVentaOutput toDetalleOutput(DetalleVenta entity);

    default String resolveDetalleNombre(DetalleVenta entity) {
        if (entity.getProducto() != null && entity.getProducto().getNombre() != null) {
            return entity.getProducto().getNombre();
        }
        return entity.getDescripcion();
    }

    default String resolveClienteNombre(Venta entity) {
        if (entity.getCliente() == null || entity.getCliente().getPersona() == null) {
            return null;
        }
        var persona = entity.getCliente().getPersona();
        String nombre = persona.getNombre() != null ? persona.getNombre() : "";
        String apellido = persona.getApellido() != null ? persona.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }
}
