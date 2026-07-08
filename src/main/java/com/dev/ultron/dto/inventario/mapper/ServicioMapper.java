package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.Servicio;
import com.dev.ultron.domain.inventario.CategoriaServicio;
import com.dev.ultron.dto.inventario.input.ServicioInput;
import com.dev.ultron.dto.inventario.output.ServicioOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {CategoriaServicioMapper.class})
public interface ServicioMapper extends BaseMapper<Servicio, ServicioInput, ServicioOutput> {

    @Mapping(target = "id_servicio", ignore = true)
    @Mapping(target = "codigo", source = "input.codigo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaServicio", source = "categoria")
    Servicio toEntity(ServicioInput input, CategoriaServicio categoria);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_servicio", ignore = true)
    @Mapping(target = "codigo", source = "input.codigo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "categoriaServicio", source = "categoria")
    void updateEntity(@MappingTarget Servicio entidad, ServicioInput input, CategoriaServicio categoria);
}
