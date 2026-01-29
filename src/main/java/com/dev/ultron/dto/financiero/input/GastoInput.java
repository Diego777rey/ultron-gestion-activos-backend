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
public class GastoInput implements Serializable {
    private Long id_movimiento;
    private String categoria;
    private String proveedor;
    private String observaciones;
}
