package com.dev.ultron.dto.patrimonio.output;

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
public class PropiedadBienOutput implements Serializable {
    private Long id_propiedad_bien;
    private Long id_bien;
    private Long id_persona;
    private BigDecimal porcentaje_propiedad;
}
