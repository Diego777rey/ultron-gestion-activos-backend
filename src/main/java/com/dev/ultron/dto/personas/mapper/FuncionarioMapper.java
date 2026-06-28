package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.dto.personas.input.FuncionarioInput;
import com.dev.ultron.dto.personas.output.FuncionarioOutput;
import com.dev.ultron.utilitarios.DateUtil;
import com.dev.ultron.utilitarios.StringUtil;

/**
 * Mapper para convertir entre Funcionario entity, input y output.
 * Reutiliza PersonaMapper para los datos de persona.
 */
public class FuncionarioMapper {

    private FuncionarioMapper() {
    }

    /**
     * Convierte un FuncionarioInput en una entidad Funcionario nueva.
     * La entidad Persona debe crearse primero y asignarse.
     */
    public static Funcionario toEntity(FuncionarioInput input, Persona persona) {
        if (input == null) return null;
        return Funcionario.builder()
                .persona(persona)
                .sueldo(input.sueldo())
                .sector(StringUtil.toUpperCase(input.sector()))
                .fechaIngreso(DateUtil.parseDate(input.fechaIngreso()))
                .facePrueba(input.facePrueba() != null ? input.facePrueba() : false)
                .estado(input.estado() != null ? input.estado() : true)
                .build();
    }

    /**
     * Actualiza una entidad Funcionario existente con datos del input.
     */
    public static void updateEntity(Funcionario funcionario, FuncionarioInput input) {
        if (input == null || funcionario == null) return;
        funcionario.setSueldo(input.sueldo());
        funcionario.setSector(StringUtil.toUpperCase(input.sector()));
        if (input.fechaIngreso() != null) {
            funcionario.setFechaIngreso(DateUtil.parseDate(input.fechaIngreso()));
        }
        if (input.facePrueba() != null) {
            funcionario.setFacePrueba(input.facePrueba());
        }
        if (input.estado() != null) {
            funcionario.setEstado(input.estado());
        }
    }

    /**
     * Convierte una entidad Funcionario a FuncionarioOutput.
     */
    public static FuncionarioOutput toOutput(Funcionario funcionario) {
        if (funcionario == null) return null;
        return FuncionarioOutput.builder()
                .id_funcionario(funcionario.getId_funcionario())
                .persona(PersonaMapper.toOutput(funcionario.getPersona()))
                .sueldo(funcionario.getSueldo())
                .sector(funcionario.getSector())
                .fechaIngreso(DateUtil.format(funcionario.getFechaIngreso()))
                .facePrueba(funcionario.isFacePrueba())
                .estado(funcionario.isEstado())
                .build();
    }
}
