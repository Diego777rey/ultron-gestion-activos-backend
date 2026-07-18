package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
import com.dev.ultron.dto.financiero.input.CajaInput;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.dto.sectores.mapper.SectorMapper;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = {PersonaMapper.class, SectorMapper.class})
public interface CajaMapper extends BaseMapper<Caja, CajaInput, CajaOutput> {

    @Mapping(target = "id_caja", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "saldo_actual", source = "input.saldoActual")
    @Mapping(target = "id_empresa", source = "input.idEmpresa")
    @Mapping(target = "responsable", source = "responsable")
    @Mapping(target = "sector", source = "sector")
    @Mapping(target = "activa", source = "input.activa")
    Caja toEntity(CajaInput input, Persona responsable, Sector sector);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_caja", ignore = true)
    @Mapping(target = "nombre", source = "input.nombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "saldo_actual", source = "input.saldoActual")
    @Mapping(target = "id_empresa", source = "input.idEmpresa")
    @Mapping(target = "responsable", source = "responsable")
    @Mapping(target = "sector", source = "sector")
    @Mapping(target = "activa", source = "input.activa")
    void updateEntity(@MappingTarget Caja entidad, CajaInput input, Persona responsable, Sector sector);

    @Mapping(target = "saldoActual", source = "saldo_actual")
    @Mapping(target = "idEmpresa", source = "id_empresa")
    @Override
    CajaOutput toOutput(Caja entity);
}
