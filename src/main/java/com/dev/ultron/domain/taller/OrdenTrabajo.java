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
import lombok.NoArgsConstructor;

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

    @Column(name = "descripcion_falla")
    private String descripcionFalla;

    @Column(name = "fecha_inicio_estimada")
    private LocalDateTime fechaInicioEstimada;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(name = "presupuesto_aprobado", nullable = false)
    private boolean presupuestoAprobado;

    @Column(name = "total_presupuesto", nullable = false)
    private BigDecimal totalPresupuesto;

    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja")
    private Caja caja;

    @OneToMany(mappedBy = "ordenTrabajo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id_detalle ASC")
    @Builder.Default
    private List<OrdenTrabajoDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (etapa == null) {
            etapa = "RECEPCION";
        }
        if (totalPresupuesto == null) {
            totalPresupuesto = BigDecimal.ZERO;
        }
    }
}
