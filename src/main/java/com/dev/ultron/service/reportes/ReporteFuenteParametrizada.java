package com.dev.ultron.service.reportes;

import java.util.List;

/**
 * Fuentes que necesitan la empresa emisora para listar, como las facturas legales.
 */
public interface ReporteFuenteParametrizada extends ReporteFuente {

    List<?> listar(String filtro, Long idEmpresa);
}
