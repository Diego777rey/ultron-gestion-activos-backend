package com.dev.ultron.dto.financiero.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaletinInput implements Serializable {
    private String nombre;
    private Long idSector;
    private Long idResponsable;
    private Boolean activo;
}
