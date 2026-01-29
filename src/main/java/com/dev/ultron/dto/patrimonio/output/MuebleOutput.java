package com.dev.ultron.dto.patrimonio.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MuebleOutput extends BienOutput {
    private String tipo_mueble;
    private String ubicacion;
    private Integer cantidad;
}
