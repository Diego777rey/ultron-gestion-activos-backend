package com.dev.ultron.dto.financiero.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalOutput implements Serializable {
    private Long id_terminal;
    private String macAddress;
    private String hostname;
    private String nombre;
    private String plataforma;
    private String ultimaConexion;
    private String fechaRegistro;
    private Boolean activa;
    private Long idCaja;
    private String cajaNombre;
}
