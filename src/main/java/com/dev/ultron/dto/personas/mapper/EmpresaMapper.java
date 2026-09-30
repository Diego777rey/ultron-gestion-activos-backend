package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.dto.personas.input.EmpresaInput;
import com.dev.ultron.dto.personas.output.EmpresaOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EmpresaMapper extends BaseMapper<Empresa, EmpresaInput, EmpresaOutput>, UpdatableMapper<Empresa, EmpresaInput> {

    @Mapping(target = "id_empresa", ignore = true)
    @Mapping(target = "razon_social", source = "razon_social", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "ruc", source = "ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "actividad_economica", source = "actividadEconomica", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "logo", source = "logo")
    @Mapping(target = "activa", source = "activa", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    @Mapping(target = "fecha_creacion", source = "fechaCreacion", qualifiedByName = MappingHelper.PARSE_DATE)
    Empresa toEntity(EmpresaInput input);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_empresa", ignore = true)
    @Mapping(target = "razon_social", source = "razon_social", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "ruc", source = "ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "direccion", source = "direccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "telefono", source = "telefono", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "actividad_economica", source = "actividadEconomica", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "logo", source = "logo")
    @Mapping(target = "fecha_creacion", source = "fechaCreacion", qualifiedByName = MappingHelper.PARSE_DATE)
    void updateEntity(@MappingTarget Empresa empresa, EmpresaInput input);

    @Override
    @Mapping(target = "fecha_creacion", source = "fecha_creacion", qualifiedByName = MappingHelper.FORMAT_DATE)
    EmpresaOutput toOutput(Empresa empresa);
}
