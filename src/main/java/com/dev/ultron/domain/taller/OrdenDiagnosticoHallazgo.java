package com.dev.ultron.domain.taller;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_diagnostico_hallazgo", schema = "taller")
public class OrdenDiagnosticoHallazgo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_hallazgo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajo ordenTrabajo;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    @Builder.Default
    private String gravedad = "MEDIA";

    private String sistema;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "etapa_origen", nullable = false)
    @Builder.Default
    private String etapaOrigen = "DIAGNOSTICO";

    @PrePersist
    protected void onCreate() {
        if (gravedad == null || gravedad.isBlank()) {
            gravedad = "MEDIA";
        }
        if (etapaOrigen == null || etapaOrigen.isBlank()) {
            etapaOrigen = "DIAGNOSTICO";
        }
    }
}
