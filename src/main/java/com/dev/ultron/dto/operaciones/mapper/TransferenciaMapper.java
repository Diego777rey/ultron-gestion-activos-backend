package com.dev.ultron.dto.operaciones.mapper;

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

    @Mapping(target = "precioVenta", source = "detalle", qualifiedByName = "extractPrecioVenta")
    TransferenciaDetalleOutput toDetalleOutput(TransferenciaDetalle detalle);

    default String formatFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null;
    }

    @Named("extractPrecioVenta")
    default BigDecimal extractPrecioVenta(TransferenciaDetalle detalle) {
        Producto producto = detalle.getProducto();
        return producto != null ? producto.getPrecioVenta() : null;
    }
}
