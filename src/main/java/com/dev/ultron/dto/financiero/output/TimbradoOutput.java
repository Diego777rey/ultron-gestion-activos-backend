package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Output DTO de Timbrado para respuestas GraphQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimbradoOutput implements Serializable {
    private Long id_timbrado;
    private String numero_timbrado;
    private String establecimiento;
    private String punto_expedicion;
    private Integer numero_inicial;
    private Integer numero_final;
    private Integer numero_actual;
    private String fecha_inicio_vigencia;
    private String fecha_fin_vigencia;
    private Long id_empresa;
    private String tipo_factura;
    private Boolean activo;
    private String fecha_creacion;
    private Integer numeros_disponibles;
    private Boolean esta_vigente;
    private Integer dias_hasta_vencimiento;
}
