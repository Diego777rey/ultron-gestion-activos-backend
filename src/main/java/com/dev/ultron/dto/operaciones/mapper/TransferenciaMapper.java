package com.dev.ultron.dto.operaciones.mapper;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.operaciones.TransferenciaDetalle;
import com.dev.ultron.dto.inventario.mapper.ProductoMapper;
import com.dev.ultron.dto.operaciones.output.TransferenciaDetalleOutput;
import com.dev.ultron.dto.operaciones.output.TransferenciaOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(config = MapStructConfig.class, uses = {SectorMapper.class, PersonaMapper.class, ProductoMapper.class})
public interface TransferenciaMapper {

    @Mapping(target = "fecha", expression = "java(formatFecha(entity.getFecha()))")
    @Mapping(target = "cantidadItems", expression = "java(entity.getDetalles() != null ? entity.getDetalles().size() : 0)")
    TransferenciaOutput toOutput(Transferencia entity);

    @Mapping(target = "idPresentacionProducto", source = "detalle", qualifiedByName = "extractPresentacionId")
    @Mapping(target = "presentacionDescripcion", source = "detalle", qualifiedByName = "extractPresentacionDescripcion")
    @Mapping(target = "cantidadPresentacion", source = "detalle", qualifiedByName = "extractCantidadPresentacion")
    @Mapping(target = "precioVenta", source = "detalle", qualifiedByName = "extractPrecioVenta")
    @Mapping(target = "cantidadTotal", source = "detalle", qualifiedByName = "calcularCantidadTotal")
    TransferenciaDetalleOutput toDetalleOutput(TransferenciaDetalle detalle);

    default String formatFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null;
    }

    @Named("extractPresentacionId")
    default Long extractPresentacionId(TransferenciaDetalle detalle) {
        PresentacionProducto pp = detalle.getPresentacionProducto();
        return pp != null ? pp.getId_presentacion_producto() : null;
    }

    @Named("extractPresentacionDescripcion")
    default String extractPresentacionDescripcion(TransferenciaDetalle detalle) {
        PresentacionProducto pp = detalle.getPresentacionProducto();
        return pp != null ? pp.getDescripcion() : null;
    }

    @Named("extractCantidadPresentacion")
    default BigDecimal extractCantidadPresentacion(TransferenciaDetalle detalle) {
        PresentacionProducto pp = detalle.getPresentacionProducto();
        return pp != null && pp.getCantidad() != null ? pp.getCantidad() : BigDecimal.ONE;
    }

    @Named("extractPrecioVenta")
    default BigDecimal extractPrecioVenta(TransferenciaDetalle detalle) {
        Producto producto = detalle.getProducto();
        if (producto == null) {
            return null;
        }
        // Si hay presentación y tiene precio propio, usar ese
        PresentacionProducto pp = detalle.getPresentacionProducto();
        if (pp != null && pp.getPrecio() != null && pp.getPrecio().compareTo(BigDecimal.ZERO) > 0) {
            return pp.getPrecio();
        }
        // Si no, usar el precioVenta del producto
        return producto.getPrecioVenta();
    }

    /**
     * Calcula la cantidad total transferida = cantidad * cantidadPresentacion.
     * Si la presentación tiene cantidad 6 y se transfiere 2, cantidadTotal = 12.
     */
    @Named("calcularCantidadTotal")
    default BigDecimal calcularCantidadTotal(TransferenciaDetalle detalle) {
        BigDecimal cantidad = detalle.getCantidad();
        if (cantidad == null) {
            return BigDecimal.ZERO;
        }
        PresentacionProducto pp = detalle.getPresentacionProducto();
        BigDecimal cantidadPres = (pp != null && pp.getCantidad() != null) ? pp.getCantidad() : BigDecimal.ONE;
        return cantidad.multiply(cantidadPres);
    }
}
