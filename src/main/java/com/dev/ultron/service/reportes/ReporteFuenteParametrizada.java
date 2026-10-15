package com.dev.ultron.service.reportes;

import java.util.List;

/**
 * Fuentes que requieren parámetros adicionales al listar (por ejemplo, la empresa emisora).
 */
public interface ReporteFuenteParametrizada extends ReporteFuente {

    List<?> listar(String filtro, Long idEmpresa);
}
