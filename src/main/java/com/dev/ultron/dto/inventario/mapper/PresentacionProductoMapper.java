package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.dto.inventario.input.PresentacionProductoInput;
import com.dev.ultron.dto.inventario.output.PresentacionProductoOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PresentacionProductoMapper extends BaseMapper<PresentacionProducto, PresentacionProductoInput, PresentacionProductoOutput> {

    @Mapping(target = "id_presentacion_producto", ignore = true)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipo", source = "input.tipo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "codigoBarras", source = "input.codigoBarras", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "principal", source = "input.principal")
    @Mapping(target = "producto", source = "producto")
    PresentacionProducto toEntity(PresentacionProductoInput input, Producto producto);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_presentacion_producto", ignore = true)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipo", source = "input.tipo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "codigoBarras", source = "input.codigoBarras", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "principal", source = "input.principal")
    @Mapping(target = "producto", source = "producto")
    void updateEntity(@MappingTarget PresentacionProducto entidad, PresentacionProductoInput input, Producto producto);
}
