package com.dev.ultron.dto.financiero.output;

import com.dev.ultron.dto.personas.output.PersonaOutput;
import com.dev.ultron.dto.sectores.output.SectorOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaletinOutput implements Serializable {
    private Long id_maletin;
    private String nombre;
    private Boolean abierto;
    private SectorOutput sector;
    private PersonaOutput responsable;
    private Boolean activo;

    /** Id de la sesión de caja abierta asociada (caja actual). */
    private Long idCajaActual;

    /** Fecha del último movimiento / sesión. */
    private String ultimoMovimiento;

    /** Último responsable que usó el maletín. */
    private PersonaOutput ultimoResponsable;
}
