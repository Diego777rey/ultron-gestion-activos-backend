package com.dev.ultron.dto.sectores.mapper;

import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.domain.sectores.Zona;
import com.dev.ultron.dto.sectores.input.ZonaInput;
import com.dev.ultron.dto.sectores.output.ZonaOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {SectorMapper.class})
public interface ZonaMapper extends BaseMapper<Zona, ZonaInput, ZonaOutput> {

    @Mapping(target = "id_zona", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "sector", source = "sector")
    Zona toEntity(ZonaInput input, Sector sector);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_zona", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado")
    @Mapping(target = "sector", source = "sector")
    void updateEntity(@MappingTarget Zona entidad, ZonaInput input, Sector sector);
}
