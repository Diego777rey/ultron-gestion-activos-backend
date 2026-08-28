package com.dev.ultron.domain.taller;

import com.dev.ultron.domain.financiero.Caja;
import com.dev.ultron.domain.patrimonio.Vehiculo;
import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Funcionario;
import com.dev.ultron.domain.personas.Usuario;
import com.dev.ultron.domain.sectores.Sector;

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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_trabajo", schema = "taller")
public class OrdenTrabajo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_orden_trabajo;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Column(nullable = false)
    private String etapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mecanico")
    private Funcionario mecanico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Usuario responsable;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja")
    private Caja caja;

    @OneToOne(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenRecepcion recepcion;

    @OneToOne(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenEstadoVehiculo estadoVehiculo;

    @OneToOne(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenDiagnostico diagnostico;

    @OneToMany(mappedBy = "ordenTrabajo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id_detalle ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrdenTrabajoDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "ordenTrabajo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id_hallazgo ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrdenDiagnosticoHallazgo> hallazgos = new ArrayList<>();

    public OrdenRecepcion ensureRecepcion() {
        if (recepcion == null) {
            recepcion = OrdenRecepcion.builder().ordenTrabajo(this).build();
        }
        return recepcion;
    }

    public OrdenEstadoVehiculo ensureEstadoVehiculo() {
        if (estadoVehiculo == null) {
            estadoVehiculo = OrdenEstadoVehiculo.builder().ordenTrabajo(this).build();
        }
        return estadoVehiculo;
    }

    public OrdenDiagnostico ensureDiagnostico() {
        if (diagnostico == null) {
            diagnostico = OrdenDiagnostico.builder()
                    .ordenTrabajo(this)
                    .presupuestoAprobado(false)
                    .totalPresupuesto(BigDecimal.ZERO)
                    .build();
        }
        return diagnostico;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (etapa == null) {
            etapa = "RECEPCION";
        }
    }
}
