package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.Factura;
import com.dev.ultron.domain.financiero.SesionCaja;
import com.dev.ultron.domain.financiero.Timbrado;
import com.dev.ultron.domain.financiero.Venta;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.input.FacturaInput;
import com.dev.ultron.dto.financiero.output.FacturaOutput;
import com.dev.ultron.dto.personas.mapper.ClienteMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {ClienteMapper.class, DetalleFacturaMapper.class})
public interface FacturaMapper {

    @Mapping(target = "id_factura", ignore = true)
    @Mapping(target = "numero_factura", ignore = true)
    @Mapping(target = "timbrado", ignore = true)
    @Mapping(target = "fecha_emision", expression = "java(com.dev.ultron.utilitarios.DateUtil.nowDateTime())")
    @Mapping(target = "timbradoEntity", source = "timbrado")
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "venta", source = "venta")
    @Mapping(target = "sesionCaja", source = "sesionCaja")
    @Mapping(target = "empresa", source = "empresa")
    @Mapping(target = "cliente_nombre", source = "input.clienteNombre", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cliente_documento", source = "input.clienteDocumento", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cliente_ruc", source = "input.clienteRuc", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "cliente_direccion", source = "input.clienteDireccion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "forma_pago", source = "input.formaPago", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "moneda", source = "input.moneda", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "observaciones", source = "input.observaciones", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "estado", constant = "EMITIDA")
    @Mapping(target = "fecha_anulacion", ignore = true)
    @Mapping(target = "motivo_anulacion", ignore = true)
    @Mapping(target = "usuario_anulacion", ignore = true)
    @Mapping(target = "usuario_emisor", source = "usuarioEmisor")
    @Mapping(target = "fecha_creacion", expression = "java(com.dev.ultron.utilitarios.DateUtil.nowDateTime())")
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "total_iva_5", ignore = true)
    @Mapping(target = "total_iva_10", ignore = true)
    @Mapping(target = "total_exenta", ignore = true)
    @Mapping(target = "total_iva", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Factura toEntity(
            FacturaInput input,
            Timbrado timbrado,
            Cliente cliente,
            Venta venta,
            SesionCaja sesionCaja,
            Empresa empresa,
            Usuario usuarioEmisor
    );

    @Mapping(target = "fecha_emision", source = "fecha_emision", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "fecha_anulacion", source = "fecha_anulacion", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "fecha_creacion", source = "fecha_creacion", qualifiedByName = MappingHelper.FORMAT_DATETIME)
    @Mapping(target = "id_timbrado", source = "timbradoEntity.id_timbrado")
    @Mapping(target = "id_cliente", source = "cliente.id_cliente")
    @Mapping(target = "id_venta", source = "venta.id_venta")
    @Mapping(target = "id_sesion_caja", source = "sesionCaja.id_sesion_caja")
    @Mapping(target = "id_empresa", source = "empresa.id_empresa")
    @Mapping(target = "id_usuario_anulacion", source = "usuario_anulacion.id")
    @Mapping(target = "id_usuario_emisor", source = "usuario_emisor.id")
    FacturaOutput toOutput(Factura factura);
}
