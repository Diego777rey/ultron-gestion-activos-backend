package com.dev.ultron.dto.patrimonio.mapper;

import com.dev.ultron.domain.patrimonio.Equipo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.EquipoInput;
import com.dev.ultron.dto.patrimonio.output.EquipoOutput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {ClienteMapper.class, VehiculoMapper.class})
public interface EquipoMapper extends BaseMapper<Equipo, EquipoInput, EquipoOutput> {

    @Mapping(target = "id_equipo", ignore = true)
    @Mapping(target = "vehiculo", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "tipoEquipo", source = "input.tipo_equipo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "marca", source = "input.marca", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "modelo", source = "input.modelo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "numeroSerie", source = "input.numero_serie", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.DEFAULT_ESTADO_ACTIVO)
    Equipo toEntity(EquipoInput input, Cliente cliente);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_equipo", ignore = true)
    @Mapping(target = "vehiculo", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "tipoEquipo", source = "input.tipo_equipo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "marca", source = "input.marca", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "modelo", source = "input.modelo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "numeroSerie", source = "input.numero_serie", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    void updateEntity(@MappingTarget Equipo equipo, EquipoInput input, Cliente cliente);

    @Override
    @Mapping(target = "tipo_equipo", source = "tipoEquipo")
    @Mapping(target = "numero_serie", source = "numeroSerie")
    @Mapping(target = "fecha_registro", source = "fechaRegistro", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    EquipoOutput toOutput(Equipo equipo);
}
