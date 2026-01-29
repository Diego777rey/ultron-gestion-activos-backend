package com.dev.ultron.dto.patrimonio.input;

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
public class MuebleInput extends BienInput {
    private String tipo_mueble;
    private String ubicacion;
    private Integer cantidad;
}
