package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.personas.UsuarioRole;
import com.dev.ultron.dto.personas.input.UsuarioInput;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.dto.personas.output.UsuarioOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Mapper(config = MapStructConfig.class, uses = {FuncionarioMapper.class, RoleMapper.class})
public abstract class UsuarioMapper implements BaseMapper<Usuario, UsuarioInput, UsuarioOutput>, UpdatableMapper<Usuario, UsuarioInput> {

    @Autowired
    protected RoleMapper roleMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "funcionario", ignore = true)
    @Mapping(target = "usuarioRoles", ignore = true)
    @Mapping(target = "reseteosContrasenha", ignore = true)
    @Mapping(target = "username", source = "input.username", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "email", source = "input.email", qualifiedByName = MappingHelper.TO_LOWER_CASE)
    @Mapping(target = "activo", source = "input.activo", qualifiedByName = MappingHelper.DEFAULT_BOOLEAN_TRUE)
    public abstract Usuario toEntity(UsuarioInput input, String encodedPassword);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "funcionario", ignore = true)
    @Mapping(target = "usuarioRoles", ignore = true)
    @Mapping(target = "reseteosContrasenha", ignore = true)
    @Mapping(target = "username", source = "username", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "email", source = "email", qualifiedByName = MappingHelper.TO_LOWER_CASE)
    public abstract void updateEntity(@MappingTarget Usuario usuario, UsuarioInput input);

    @Override
    @Mapping(target = "id_funcionario", source = "funcionario.id_funcionario")
    @Mapping(target = "roles", ignore = true)
    public abstract UsuarioOutput toOutput(Usuario usuario);

    @AfterMapping
    protected void fillRoles(@MappingTarget UsuarioOutput output, Usuario usuario) {
        output.setRoles(mapRoles(usuario.getUsuarioRoles()));
    }

    protected List<RoleOutput> mapRoles(List<UsuarioRole> usuarioRoles) {
        if (usuarioRoles == null || usuarioRoles.isEmpty()) {
            return Collections.emptyList();
        }
        return usuarioRoles.stream()
                .map(UsuarioRole::getRole)
                .map(roleMapper::toOutput)
                .toList();
    }
}
