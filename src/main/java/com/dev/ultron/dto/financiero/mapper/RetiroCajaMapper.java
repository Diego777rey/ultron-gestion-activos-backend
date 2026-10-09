package com.dev.ultron.dto.financiero.mapper;

import com.dev.ultron.domain.financiero.RetiroCaja;
import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.dto.financiero.output.RetiroCajaOutput;
import com.dev.ultron.generic.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface RetiroCajaMapper {

    @Mapping(target = "idSesionCaja", source = "sesionCaja.id_sesion_caja")
    @Mapping(target = "idUsuarioResponsable", source = "responsable.id")
    @Mapping(target = "responsableUsuario", source = "responsable.username")
    @Mapping(target = "responsableNombre", expression = "java(nombreUsuario(entity.getResponsable()))")
    @Mapping(target = "registradoPor", expression = "java(nombreUsuario(entity.getRegistradoPor()))")
    RetiroCajaOutput toOutput(RetiroCaja entity);

    default String nombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        Persona persona = usuario.getFuncionario() != null ? usuario.getFuncionario().getPersona() : null;
        if (persona != null) {
            String nombre = ((persona.getNombre() != null ? persona.getNombre() : "")
                    + " "
                    + (persona.getApellido() != null ? persona.getApellido() : "")).trim();
            if (!nombre.isEmpty()) {
                return nombre;
            }
        }
        return usuario.getUsername();
    }
}
