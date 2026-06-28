package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.ClienteInput;
import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.utilitarios.DateUtil;
import com.dev.ultron.utilitarios.StringUtil;

/**
 * Mapper para convertir entre Cliente entity, input y output.
 * Reutiliza PersonaMapper para los datos de persona.
 */
public class ClienteMapper {

    private ClienteMapper() {
    }

    /**
     * Convierte un ClienteInput en una entidad Cliente nueva.
     * La entidad Persona debe crearse primero y asignarse.
     */
    public static Cliente toEntity(ClienteInput input, Persona persona) {
        if (input == null) return null;
        return Cliente.builder()
                .persona(persona)
                .ruc(input.ruc())
                .tipoCliente(StringUtil.toUpperCase(input.tipoCliente()))
                .limiteCredito(input.limiteCredito())
                .fechaRegistro(DateUtil.parseDate(input.fechaRegistro()))
                .observaciones(StringUtil.toUpperCase(input.observaciones()))
                .estado(input.estado() != null ? input.estado() : true)
                .build();
    }

    /**
     * Actualiza una entidad Cliente existente con datos del input.
     */
    public static void updateEntity(Cliente cliente, ClienteInput input) {
        if (input == null || cliente == null) return;
        cliente.setRuc(input.ruc());
        cliente.setTipoCliente(StringUtil.toUpperCase(input.tipoCliente()));
        cliente.setLimiteCredito(input.limiteCredito());
        if (input.fechaRegistro() != null) {
            cliente.setFechaRegistro(DateUtil.parseDate(input.fechaRegistro()));
        }
        cliente.setObservaciones(StringUtil.toUpperCase(input.observaciones()));
        if (input.estado() != null) {
            cliente.setEstado(input.estado());
        }
    }

    /**
     * Convierte una entidad Cliente a ClienteOutput.
     */
    public static ClienteOutput toOutput(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteOutput.builder()
                .id_cliente(cliente.getId_cliente())
                .persona(PersonaMapper.toOutput(cliente.getPersona()))
                .ruc(cliente.getRuc())
                .tipoCliente(cliente.getTipoCliente())
                .limiteCredito(cliente.getLimiteCredito())
                .fechaRegistro(DateUtil.format(cliente.getFechaRegistro()))
                .observaciones(cliente.getObservaciones())
                .estado(cliente.isEstado())
                .build();
    }
}
