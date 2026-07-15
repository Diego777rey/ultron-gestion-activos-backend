package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.CategoriaProducto;
import com.dev.ultron.dto.inventario.input.ProductoInput;
import com.dev.ultron.dto.inventario.output.ProductoOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {CategoriaProductoMapper.class, PresentacionProductoMapper.class})
public interface ProductoMapper extends BaseMapper<Producto, ProductoInput, ProductoOutput> {

    @Mapping(target = "id_producto", ignore = true)
    @Mapping(target = "presentaciones", ignore = true)
    @Mapping(target = "codigo", source = "input.codigo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "ubicacion", source = "input.ubicacion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "categoriaProducto", source = "categoria")
    Producto toEntity(ProductoInput input, CategoriaProducto categoria);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_producto", ignore = true)
    @Mapping(target = "presentaciones", ignore = true)
    @Mapping(target = "codigo", source = "input.codigo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "ubicacion", source = "input.ubicacion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "categoriaProducto", source = "categoria")
    void updateEntity(@MappingTarget Producto entidad, ProductoInput input, CategoriaProducto categoria);
}
