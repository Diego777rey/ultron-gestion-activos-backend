package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.Maletin;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.financiero.input.MaletinInput;
import com.dev.ultron.dto.financiero.output.MaletinOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = PersonaMapper.class)
public interface MaletinMapper extends BaseMapper<Maletin, MaletinInput, MaletinOutput> {

    @Mapping(target = "id_maletin", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "responsable", source = "responsable")
    Maletin toEntity(MaletinInput input, Persona responsable);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_maletin", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "responsable", source = "responsable")
    void updateEntity(@MappingTarget Maletin entidad, MaletinInput input, Persona responsable);
}
