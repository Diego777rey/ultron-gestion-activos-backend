package com.dev.ultron.dto.operaciones.output;

import com.dev.ultron.dto.personas.output.PersonaOutput;
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
public class TransferenciaOutput implements Serializable {
    private Long id_transferencia;
    private String numero;
    private SectorOutput sectorOrigen;
    private SectorOutput sectorDestino;
    private String observacion;
    private String estado;
    private String fecha;
    private PersonaOutput persona;
    private Integer cantidadItems;
    private List<TransferenciaDetalleOutput> detalles;
}
