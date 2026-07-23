package com.dev.ultron.domain.taller;

import com.dev.ultron.domain.operaciones.Transferencia;
import com.dev.ultron.domain.sectores.Sector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitud_repuesto", schema = "taller")
public class SolicitudRepuesto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_solicitud_repuesto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sector_origen", nullable = false)
    private Sector sectorOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sector_destino", nullable = false)
    private Sector sectorDestino;

    @Column(nullable = false)
    private String estado;

    private String observacion;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia")
    private Transferencia transferencia;

    @OneToMany(mappedBy = "solicitudRepuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id_detalle ASC")
    @Builder.Default
    private List<SolicitudRepuestoDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
