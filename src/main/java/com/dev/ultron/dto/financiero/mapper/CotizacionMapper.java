package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.Cotizacion;
import com.dev.ultron.dto.financiero.input.CotizacionInput;
import com.dev.ultron.dto.financiero.output.CotizacionOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CotizacionMapper extends BaseMapper<Cotizacion, CotizacionInput, CotizacionOutput> {

    @Mapping(target = "id_cotizacion", ignore = true)
    @Mapping(target = "moneda", source = "moneda")
    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activa", source = "activa")
    Cotizacion toEntity(CotizacionInput input);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_cotizacion", ignore = true)
    @Mapping(target = "moneda", source = "moneda")
    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activa", source = "activa")
    void updateEntity(@MappingTarget Cotizacion entidad, CotizacionInput input);

    @Override
    CotizacionOutput toOutput(Cotizacion entity);
}
