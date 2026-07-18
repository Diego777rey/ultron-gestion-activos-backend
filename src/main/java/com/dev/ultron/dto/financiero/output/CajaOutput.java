package com.dev.ultron.dto.financiero.output;

import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaOutput implements Serializable {
    private Long id_caja;
    private String nombre;
    private BigDecimal saldoActual;
    private Long idEmpresa;
    private PersonaOutput responsable;
    private SectorOutput sector;
    private Boolean activa;
}
