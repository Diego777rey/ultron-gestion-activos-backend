package com.dev.ultron.dto.inventario.mapper;

import com.dev.ultron.domain.inventario.Presentacion;
import com.dev.ultron.dto.inventario.input.PresentacionInput;
import com.dev.ultron.dto.inventario.output.PresentacionOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PresentacionMapper extends BaseMapper<Presentacion, PresentacionInput, PresentacionOutput> {

    @Mapping(target = "id_presentacion", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "estado", source = "estado")
    Presentacion toEntity(PresentacionInput input);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_presentacion", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cantidad", source = "cantidad")
    @Mapping(target = "estado", source = "estado")
    void updateEntity(@MappingTarget Presentacion entidad, PresentacionInput input);
}
