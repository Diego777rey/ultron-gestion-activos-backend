package com.dev.ultron.dto.taller.output;

import com.dev.ultron.dto.personas.output.ClienteOutput;
import com.dev.ultron.dto.personas.output.FuncionarioOutput;
import com.dev.ultron.dto.personas.output.UsuarioOutput;
import com.dev.ultron.dto.patrimonio.output.VehiculoOutput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import com.dev.ultron.dto.financiero.output.CajaOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoOutput implements Serializable {
    private Long id_orden_trabajo;
    private String numero_orden;
    private String etapa;
    private ClienteOutput cliente;
    private VehiculoOutput vehiculo;
    private FuncionarioOutput mecanico;
    private SectorOutput sector;
    private UsuarioOutput responsable;
    private String descripcion_falla;
    private String fecha_inicio_estimada;
    private String fecha_fin_estimada;
    private String fecha_creacion;
    private String fecha_finalizacion;
    private Boolean presupuesto_aprobado;
    private BigDecimal total_presupuesto;
    private String observaciones;
    private CajaOutput caja;
    private List<OrdenTrabajoDetalleOutput> detalles;
}
