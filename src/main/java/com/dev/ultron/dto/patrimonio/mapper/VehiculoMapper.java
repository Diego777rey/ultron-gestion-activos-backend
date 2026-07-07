package com.dev.ultron.dto.patrimonio.mapper;

import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.dto.patrimonio.input.VehiculoInput;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = ClienteMapper.class)
public interface VehiculoMapper extends BaseMapper<Vehiculo, VehiculoInput, VehiculoOutput> {

    @Mapping(target = "id_bien", ignore = true)
    @Mapping(target = "tipo", constant = "VEHICULO")
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.DEFAULT_ESTADO_ACTIVO)
    @Mapping(target = "marca", source = "input.marca", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "modelo", source = "input.modelo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "chapa", source = "input.chapa", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipoVehiculo", source = "input.tipo_vehiculo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    Vehiculo toEntity(VehiculoInput input, Cliente cliente);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_bien", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "descripcion", source = "input.descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", source = "input.estado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "marca", source = "input.marca", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "modelo", source = "input.modelo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "chapa", source = "input.chapa", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "tipoVehiculo", source = "input.tipo_vehiculo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    void updateEntity(@MappingTarget Vehiculo vehiculo, VehiculoInput input, Cliente cliente);

    @Override
    @Mapping(target = "tipo_vehiculo", source = "tipoVehiculo")
    VehiculoOutput toOutput(Vehiculo vehiculo);
}
