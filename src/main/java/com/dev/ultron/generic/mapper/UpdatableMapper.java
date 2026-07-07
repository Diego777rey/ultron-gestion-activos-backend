package com.dev.ultron.generic.mapper;

import org.mapstruct.MappingTarget;

/**
 * Extensión para mappers cuya actualización solo requiere entidad + input.
 */
public interface UpdatableMapper<E, I> {

    void updateEntity(@MappingTarget E entity, I input);
}
