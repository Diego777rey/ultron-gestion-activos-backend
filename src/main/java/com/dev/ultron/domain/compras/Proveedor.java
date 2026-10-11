package com.dev.ultron.domain.compras;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedor", schema = "compras")
public class Proveedor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_proveedor;

    @Column(name = "tipo_persona", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoPersona tipo_persona;

    // Campos para persona física
    private String nombre;
    private String apellido;

    // Campos para persona jurídica
    private String razon_social;
    private String nombre_comercial;
    private String nombre_fantasia;

    // Campos comunes
    @Column(nullable = false, unique = true, length = 50)
    private String ruc;
    
    private String documento;
    private String direccion;
    private String telefono;
    private String email;
    private String ciudad;
    private String pais;
    private String codigo_postal;
    private String sitio_web;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // Control
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fecha_registro;
    
    @Column(name = "fecha_ultima_modificacion")
    private LocalDateTime fecha_ultima_modificacion;

    @PrePersist
    protected void onCreate() {
        fecha_registro = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fecha_ultima_modificacion = LocalDateTime.now();
    }

    public enum TipoPersona {
        FISICA,
        JURIDICA
    }
}
