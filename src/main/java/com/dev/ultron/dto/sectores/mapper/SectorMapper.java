package com.dev.ultron.dto.sectores.mapper;

import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.sectores.input.SectorInput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface SectorMapper extends BaseMapper<Sector, SectorInput, SectorOutput> {

    @Mapping(target = "id_sector", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "estado")
    Sector toEntity(SectorInput input);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_sector", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "estado")
    void updateEntity(@MappingTarget Sector entidad, SectorInput input);
}
