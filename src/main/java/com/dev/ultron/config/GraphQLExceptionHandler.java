package com.dev.ultron.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dev.ultron.generic.EntityNotFoundException;

/**
 * Manejador global de excepciones para GraphQL.
 *
 * Convierte las excepciones del dominio en errores GraphQL consistentes que el
 * frontend puede transformar en avisos precisos. Cada error incluye:
 * <ul>
 *     <li>Un mensaje legible para el usuario.</li>
 *     <li>Un {@link ErrorType} que expone la {@code classification} estándar.</li>
 *     <li>Extensiones {@code code} (código estable de negocio) y {@code timestamp}.</li>
 * </ul>
 */
@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof EntityNotFoundException) {
            return buildError(env, ex.getMessage(), ErrorType.NOT_FOUND, "ENTITY_NOT_FOUND");
        }

        if (ex instanceof IllegalArgumentException) {
            return buildError(env, ex.getMessage(), ErrorType.BAD_REQUEST, "VALIDATION_ERROR");
        }

        if (ex instanceof DataIntegrityViolationException) {
            return buildError(
                    env,
                    "No se pudo completar la operación porque afectaría la integridad de los datos "
                            + "(por ejemplo, un registro relacionado o un valor duplicado).",
                    ErrorType.BAD_REQUEST,
                    "DATA_INTEGRITY_VIOLATION");
        }

        return buildError(
                env,
                "Ocurrió un error interno en el servidor. Inténtelo nuevamente más tarde.",
                ErrorType.INTERNAL_ERROR,
                "INTERNAL_ERROR");
    }

    /**
     * Construye un error GraphQL con clasificación y extensiones estándar.
     */
    private GraphQLError buildError(DataFetchingEnvironment env,
                                    String message,
                                    ErrorType errorType,
                                    String code) {
        Map<String, Object> extensions = new LinkedHashMap<>();
        extensions.put("code", code);
        extensions.put("timestamp", OffsetDateTime.now().toString());

        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(errorType)
                .extensions(extensions)
                .build();
    }
}
