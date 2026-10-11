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
public class ProveedorOutput implements Serializable {
    private Long id_proveedor;
    private String tipo_persona;
    private String nombre;
    private String apellido;
    private String razon_social;
    private String nombre_comercial;
    private String nombre_fantasia;
    private String ruc;
    private String documento;
    private String direccion;
    private String telefono;
    private String email;
    private String ciudad;
    private String pais;
    private String codigo_postal;
    private String sitio_web;
    private String observaciones;
    private Boolean activo;
    private String fecha_registro;
    private String fecha_ultima_modificacion;
    private String nombre_completo;
}
