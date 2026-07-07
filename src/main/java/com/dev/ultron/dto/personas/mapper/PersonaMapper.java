package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PersonaMapper extends BaseMapper<Persona, PersonaInput, PersonaOutput>, UpdatableMapper<Persona, PersonaInput> {

    @Mapping(target = "id_persona", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "email", source = "email", qualifiedByName = MappingHelper.TO_LOWER_CASE)
    @Mapping(target = "telefono", source = "telefono", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "estado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    Persona toEntity(PersonaInput input);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_persona", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "apellido", source = "apellido", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "documento", source = "documento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "email", source = "email", qualifiedByName = MappingHelper.TO_LOWER_CASE)
    @Mapping(target = "telefono", source = "telefono", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "estado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    void updateEntity(@MappingTarget Persona persona, PersonaInput input);
}
