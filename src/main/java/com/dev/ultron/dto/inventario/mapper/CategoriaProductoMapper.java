package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.CategoriaProducto;
import com.dev.ultron.dto.inventario.input.CategoriaProductoInput;
import com.dev.ultron.dto.inventario.output.CategoriaProductoOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CategoriaProductoMapper extends BaseMapper<CategoriaProducto, CategoriaProductoInput, CategoriaProductoOutput> {

    @Mapping(target = "id_categoria_producto", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaPadre", source = "padre")
    CategoriaProducto toEntity(CategoriaProductoInput input, CategoriaProducto padre);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_categoria_producto", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaPadre", source = "padre")
    void updateEntity(@MappingTarget CategoriaProducto entidad, CategoriaProductoInput input, CategoriaProducto padre);
}
