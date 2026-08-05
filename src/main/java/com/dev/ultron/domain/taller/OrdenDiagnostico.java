package com.dev.ultron.domain.taller;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_diagnostico", schema = "taller")
public class OrdenDiagnostico implements Serializable {

    @Id
    @Column(name = "id_orden_trabajo")
    private Long idOrdenTrabajo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_orden_trabajo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "fecha_inicio_estimada")
    private LocalDateTime fechaInicioEstimada;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "presupuesto_aprobado", nullable = false)
    @Builder.Default
    private boolean presupuestoAprobado = false;

    @Column(name = "total_presupuesto", nullable = false)
    @Builder.Default
    private BigDecimal totalPresupuesto = BigDecimal.ZERO;

    private String observaciones;

    @PrePersist
    protected void onCreate() {
        if (totalPresupuesto == null) {
            totalPresupuesto = BigDecimal.ZERO;
        }
    }
}
