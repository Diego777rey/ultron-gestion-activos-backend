package com.dev.ultron.dto.taller.output;

import com.dev.ultron.dto.sectores.output.SectorOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRepuestoOutput implements Serializable {
    private Long id_solicitud_repuesto;
    private Long id_orden_trabajo;
    private String numero_orden;
    private SectorOutput sector_origen;
    private SectorOutput sector_destino;
    private String estado;
    private String observacion;
    private String motivo_rechazo;
    private String fecha;
    private Long id_transferencia;
    private String numero_transferencia;
    private List<SolicitudRepuestoDetalleOutput> detalles;
}
