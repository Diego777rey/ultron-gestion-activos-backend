package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.FuncionarioInput;
import com.dev.ultron.dto.personas.output.FuncionarioOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = PersonaMapper.class)
public interface FuncionarioMapper extends BaseMapper<Funcionario, FuncionarioInput, FuncionarioOutput>, UpdatableMapper<Funcionario, FuncionarioInput> {

    @Mapping(target = "id_funcionario", ignore = true)
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "sector", source = "input.sector", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "fechaIngreso", source = "input.fechaIngreso", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "facePrueba", source = "input.facePrueba", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_FALSE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    Funcionario toEntity(FuncionarioInput input, Persona persona);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_funcionario", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "sector", source = "sector", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "fechaIngreso", source = "fechaIngreso", qualifiedByName = MappingHelper.PARSE_DATE)
    void updateEntity(@MappingTarget Funcionario funcionario, FuncionarioInput input);

    @Override
    @Mapping(target = "fechaIngreso", source = "fechaIngreso", qualifiedByName = MappingHelper.FORMAT_DATE)
    FuncionarioOutput toOutput(Funcionario funcionario);
}
