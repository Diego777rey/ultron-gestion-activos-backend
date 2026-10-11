package com.dev.ultron.dto.compras.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendedorOutput implements Serializable {
    private Long id_vendedor;
    private Long id_proveedor;
    private String proveedor_nombre;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String celular;
    private String cargo;
    private String observaciones;
    private Boolean activo;
    private String fecha_registro;
    private String fecha_ultima_modificacion;
    private String nombre_completo;
}
