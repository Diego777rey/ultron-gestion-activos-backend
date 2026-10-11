package com.dev.ultron.domain.compras;

import com.dev.ultron.domain.personas.Persona;
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
@Table(name = "vendedor", schema = "compras")
public class Vendedor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    private String documento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private String telefono;
    private String email;
    private String celular;
    private String cargo;

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
}
