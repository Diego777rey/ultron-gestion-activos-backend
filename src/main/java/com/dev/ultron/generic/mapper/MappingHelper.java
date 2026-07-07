package com.dev.ultron.generic.mapper;

import com.dev.ultron.utilitarios.AppConstants;
import com.dev.ultron.utilitarios.DateUtil;
import com.dev.ultron.utilitarios.StringUtil;
import org.mapstruct.Named;

import java.time.LocalDate;

/**
 * Transformaciones reutilizables para MapStruct.
 * Regla de negocio: todo String de texto va en MAYÚSCULAS, excepto email en minúsculas.
 * Para consultas y búsquedas usar {@link com.dev.ultron.generic.SearchNormalizer}.
 */
public final class MappingHelper {

    public static final String TO_UPPER_CASE = "toUpperCase";
    public static final String TO_LOWER_CASE = "toLowerCase";
    public static final String PARSE_DATE = "parseDate";
    public static final String FORMAT_DATE = "formatDate";
    public static final String DEFAULT_ESTADO_ACTIVO = "defaultEstadoActivo";
    public static final String DEFAULT_BOOLEAN_TRUE = "defaultBooleanTrue";
    public static final String DEFAULT_BOOLEAN_FALSE = "defaultBooleanFalse";
    public static final String DEFAULT_ROLE_ACTIVO = "defaultRoleActivo";

    private MappingHelper() {
    }

    @Named(TO_UPPER_CASE)
    public static String toUpperCase(String value) {
        return StringUtil.toUpperCase(value);
    }

    @Named(TO_LOWER_CASE)
    public static String toLowerCase(String value) {
        return StringUtil.toLowerCase(value);
    }

    @Named(PARSE_DATE)
    public static LocalDate parseDate(String value) {
        return DateUtil.parseDate(value);
    }

    @Named(FORMAT_DATE)
    public static String formatDate(LocalDate value) {
        return DateUtil.format(value);
    }

    @Named(DEFAULT_ESTADO_ACTIVO)
    public static String defaultEstadoActivo(String estado) {
        if (StringUtil.isNullOrEmpty(estado)) {
            return AppConstants.Estados.ACTIVO;
        }
        return StringUtil.toUpperCase(estado);
    }

    @Named(DEFAULT_BOOLEAN_TRUE)
    public static boolean defaultBooleanTrue(Boolean value) {
        return value != null ? value : true;
    }

    @Named(DEFAULT_BOOLEAN_FALSE)
    public static boolean defaultBooleanFalse(Boolean value) {
        return value != null ? value : false;
    }

    @Named(DEFAULT_ROLE_ACTIVO)
    public static String defaultRoleActivo(String activo) {
        if (activo == null) {
            return "ACTIVO";
        }
        return StringUtil.toUpperCase(activo);
    }
}
