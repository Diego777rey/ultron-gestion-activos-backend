package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.Timbrado;
import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.TimbradoInput;
import com.dev.ultron.dto.financiero.output.TimbradoOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(config = MapStructConfig.class)
public interface TimbradoMapper extends BaseMapper<Timbrado, TimbradoInput, TimbradoOutput>, UpdatableMapper<Timbrado, TimbradoInput> {

    @Mapping(target = "id_timbrado", ignore = true)
    @Mapping(target = "empresa", source = "empresa")
    @Mapping(target = "usuario_creador", source = "usuarioCreador")
    @Mapping(target = "numero_timbrado", source = "input.numeroTimbrado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "establecimiento", source = "input.establecimiento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "punto_expedicion", source = "input.puntoExpedicion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "numero_inicial", source = "input.numeroInicial")
    @Mapping(target = "numero_final", source = "input.numeroFinal")
    @Mapping(target = "numero_actual", source = "input.numeroActual")
    @Mapping(target = "fecha_inicio_vigencia", source = "input.fechaInicioVigencia", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "fecha_fin_vigencia", source = "input.fechaFinVigencia", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "tipo_factura", source = "input.tipoFactura", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "activo", source = "input.activo", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    @Mapping(target = "fecha_creacion", expression = "java(com.dev.ultron.utilitarios.DateUtil.nowDateTime())")
    Timbrado toEntity(TimbradoInput input, Empresa empresa, Usuario usuarioCreador);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id_timbrado", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "usuario_creador", ignore = true)
    @Mapping(target = "fecha_creacion", ignore = true)
    @Mapping(target = "numero_timbrado", source = "numeroTimbrado", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "establecimiento", source = "establecimiento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "punto_expedicion", source = "puntoExpedicion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "numero_inicial", source = "numeroInicial")
    @Mapping(target = "numero_final", source = "numeroFinal")
    @Mapping(target = "numero_actual", source = "numeroActual")
    @Mapping(target = "fecha_inicio_vigencia", source = "fechaInicioVigencia", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "fecha_fin_vigencia", source = "fechaFinVigencia", qualifiedByName = MappingHelper.PARSE_DATE)
    @Mapping(target = "tipo_factura", source = "tipoFactura", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    void updateEntity(@MappingTarget Timbrado timbrado, TimbradoInput input);

    @Override
    @Mapping(target = "fecha_inicio_vigencia", source = "fecha_inicio_vigencia", qualifiedByName = MappingHelper.FORMAT_DATE)
    @Mapping(target = "fecha_fin_vigencia", source = "fecha_fin_vigencia", qualifiedByName = MappingHelper.FORMAT_DATE)
    @Mapping(target = "fecha_creacion", source = "fecha_creacion", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "id_empresa", source = "empresa.id_empresa")
    @Mapping(target = "numeros_disponibles", expression = "java(calcularNumerosDisponibles(timbrado))")
    @Mapping(target = "esta_vigente", expression = "java(estaVigente(timbrado))")
    @Mapping(target = "dias_hasta_vencimiento", expression = "java(calcularDiasHastaVencimiento(timbrado))")
    TimbradoOutput toOutput(Timbrado timbrado);

    default Integer calcularNumerosDisponibles(Timbrado timbrado) {
        if (timbrado == null || timbrado.getNumero_final() == null || timbrado.getNumero_actual() == null) {
            return 0;
        }
        return timbrado.getNumero_final() - timbrado.getNumero_actual() + 1;
    }

    default Boolean estaVigente(Timbrado timbrado) {
        if (timbrado == null || timbrado.getFecha_inicio_vigencia() == null || timbrado.getFecha_fin_vigencia() == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(timbrado.getFecha_inicio_vigencia()) && !hoy.isAfter(timbrado.getFecha_fin_vigencia());
    }

    default Integer calcularDiasHastaVencimiento(Timbrado timbrado) {
        if (timbrado == null || timbrado.getFecha_fin_vigencia() == null) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(timbrado.getFecha_fin_vigencia())) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(hoy, timbrado.getFecha_fin_vigencia());
    }
}
