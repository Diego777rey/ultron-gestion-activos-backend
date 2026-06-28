package com.dev.ultron.generic;

/**
 * Excepción lanzada cuando no se encuentra una entidad en la base de datos.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
