package com.dev.ultron.dto.personas.mapper;

import com.dev.ultron.domain.personas.Role;
import com.dev.ultron.dto.personas.input.RoleInput;
import com.dev.ultron.dto.personas.output.RoleOutput;
import com.dev.ultron.generic.mapper.BaseMapper;
import com.dev.ultron.generic.mapper.MapStructConfig;
import com.dev.ultron.generic.mapper.MappingHelper;
import com.dev.ultron.generic.mapper.UpdatableMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper extends BaseMapper<Role, RoleInput, RoleOutput>, UpdatableMapper<Role, RoleInput> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRoles", ignore = true)
    @Mapping(target = "rolePermisos", ignore = true)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "activo", source = "activo", qualifiedByName = MappingHelper.DEFAULT_ROLE_ACTIVO)
    Role toEntity(RoleInput input);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRoles", ignore = true)
    @Mapping(target = "rolePermisos", ignore = true)
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    @Mapping(target = "activo", source = "activo", qualifiedByName = MappingHelper.TO_UPPER_CASE)
    void updateEntity(@MappingTarget Role role, RoleInput input);
}
