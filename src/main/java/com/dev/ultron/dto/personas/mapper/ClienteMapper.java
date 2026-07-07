package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.ClienteInput;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = PersonaMapper.class)
public interface ClienteMapper extends BaseMapper<Cliente, ClienteInput, ClienteOutput>, UpdatableMapper<Cliente, ClienteInput> {

    @Mapping(target = "id_cliente", ignore = true)
    @Mapping(target = "vehiculos", ignore = true)
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "ruc", source = "input.ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipoCliente", source = "input.tipoCliente", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "observaciones", source = "input.observaciones", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "fechaRegistro", source = "input.fechaRegistro", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    Cliente toEntity(ClienteInput input, Persona persona);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_cliente", ignore = true)
    @Mapping(target = "vehiculos", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "ruc", source = "ruc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipoCliente", source = "tipoCliente", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "observaciones", source = "observaciones", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "fechaRegistro", source = "fechaRegistro", qualifiedByName = MappingHelper.PARSE_DATE)
    void updateEntity(@MappingTarget Cliente cliente, ClienteInput input);

    @Override
    @Mapping(target = "fechaRegistro", source = "fechaRegistro", qualifiedByName = MappingHelper.FORMAT_DATE)
    ClienteOutput toOutput(Cliente cliente);

    /**
     * Crea un cliente mínimo a partir de una persona ya persistida.
     * Usado al registrar funcionarios para garantizar un registro de cliente asociado.
     */
    @Mapping(target = "id_cliente", ignore = true)
    @Mapping(target = "vehiculos", ignore = true)
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "ruc", source = "persona.documento")
    @Mapping(target = "tipoCliente", constant = "PERSONA FISICA")
    @Mapping(target = "limiteCredito", ignore = true)
    @Mapping(target = "fechaRegistro", expression = "java(com.dev.ultron.utilitarios.DateUtil.now())")
    @Mapping(target = "observaciones", ignore = true)
    @Mapping(target = "estado", constant = "true")
    Cliente toAutomaticoFromPersona(Persona persona);
}
