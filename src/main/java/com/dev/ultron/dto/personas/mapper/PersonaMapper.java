package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.PersonaInput;
import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.utilitarios.StringUtil;

/**
 * Mapper centralizado para convertir entre Persona entity, input y output.
 * Reutilizable desde cualquier módulo que necesite mapear datos de persona.
 */
public class PersonaMapper {

    private PersonaMapper() {
    }

    /**
     * Convierte un PersonaInput en una entidad Persona nueva.
     */
    public static Persona toEntity(PersonaInput input) {
        if (input == null) return null;
        return Persona.builder()
                .nombre(StringUtil.toUpperCase(input.nombre()))
                .apellido(StringUtil.toUpperCase(input.apellido()))
                .documento(input.documento())
                .email(StringUtil.toLowerCase(input.email()))
                .telefono(input.telefono())
                .direccion(input.direccion())
                .estado(input.estado())
                .build();
    }

    /**
     * Actualiza una entidad Persona existente con datos del input.
     */
    public static void updateEntity(Persona persona, PersonaInput input) {
        if (input == null || persona == null) return;
        persona.setNombre(StringUtil.toUpperCase(input.nombre()));
        persona.setApellido(StringUtil.toUpperCase(input.apellido()));
        persona.setDocumento(input.documento());
        persona.setEmail(StringUtil.toLowerCase(input.email()));
        persona.setTelefono(input.telefono());
        persona.setDireccion(input.direccion());
        if (input.estado() != null) {
            persona.setEstado(input.estado());
        }
    }

    /**
     * Convierte una entidad Persona a PersonaOutput.
     */
    public static PersonaOutput toOutput(Persona persona) {
        if (persona == null) return null;
        return PersonaOutput.builder()
                .id_persona(persona.getId_persona())
                .nombre(persona.getNombre())
                .apellido(persona.getApellido())
                .documento(persona.getDocumento())
                .email(persona.getEmail())
                .telefono(persona.getTelefono())
                .direccion(persona.getDireccion())
                .estado(persona.getEstado())
                .build();
    }
}
