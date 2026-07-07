package com.dev.ultron.generic.mapper;

/**
 * Contrato base de mapeo reutilizable en todo el proyecto.
 * Los mappers concretos extienden esta interfaz y definen {@code toEntity}
 * y {@code updateEntity} según sus necesidades (un solo input o parámetros adicionales).
 *
 * @param <E> Entidad JPA
 * @param <I> DTO de entrada
 * @param <O> DTO de salida
 */
public interface BaseMapper<E, I, O> {

    O toOutput(E entity);
}
