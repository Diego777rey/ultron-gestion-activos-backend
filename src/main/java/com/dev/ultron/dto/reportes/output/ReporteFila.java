package com.dev.ultron.dto.reportes.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila genérica del reporte de inventario. Los nombres de campo coinciden
 * con los {@code <field>} de {@code reports/inventario_generico.jrxml}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFila {

    private Integer numero;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String subcategoria;
    private String precio;
    private String extra;
    private String estado;
}
