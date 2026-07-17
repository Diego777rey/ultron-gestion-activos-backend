package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.ConteoDenominacion;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.dto.financiero.output.ConteoDenominacionOutput;
import com.dev.ultron.dto.financiero.output.SesionCajaOutput;
import com.dev.ultron.dto.personas.mapper.PersonaMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {CajaMapper.class, MaletinMapper.class, PersonaMapper.class})
public interface SesionCajaMapper {

    SesionCajaOutput toOutput(SesionCaja entity);

    @Mapping(target = "valorDenominacion", source = "valorDenominacion")
    ConteoDenominacionOutput toConteoOutput(ConteoDenominacion entity);
}
