package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.CategoriaServicio;
import com.dev.ultron.dto.inventario.input.CategoriaServicioInput;
import com.dev.ultron.dto.inventario.output.CategoriaServicioOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CategoriaServicioMapper extends BaseMapper<CategoriaServicio, CategoriaServicioInput, CategoriaServicioOutput> {

    @Mapping(target = "id_categoria_servicio", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaPadre", source = "padre")
    CategoriaServicio toEntity(CategoriaServicioInput input, CategoriaServicio padre);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_categoria_servicio", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaPadre", source = "padre")
    void updateEntity(@MappingTarget CategoriaServicio entidad, CategoriaServicioInput input, CategoriaServicio padre);
}
