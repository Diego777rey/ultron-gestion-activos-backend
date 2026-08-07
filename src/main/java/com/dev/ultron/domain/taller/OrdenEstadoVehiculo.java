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
@Table(name = "orden_estado_vehiculo", schema = "taller")
public class OrdenEstadoVehiculo implements Serializable {

    @Id
    @Column(name = "id_orden_trabajo")
    private Long idOrdenTrabajo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_orden_trabajo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "falla_mecanica", nullable = false)
    @Builder.Default
    private boolean fallaMecanica = false;

    @Column(name = "falla_electrica", nullable = false)
    @Builder.Default
    private boolean fallaElectrica = false;

    @Column(name = "estado_llantas", nullable = false)
    @Builder.Default
    private boolean estadoLlantas = false;

    @Column(name = "estado_pintura", nullable = false)
    @Builder.Default
    private boolean estadoPintura = false;

    @Column(name = "estado_rayones", nullable = false)
    @Builder.Default
    private boolean estadoRayones = false;

    @Column(name = "estado_golpes", nullable = false)
    @Builder.Default
    private boolean estadoGolpes = false;

    @Column(name = "estado_vidrios", nullable = false)
    @Builder.Default
    private boolean estadoVidrios = false;

    @Column(name = "perdida_aceite", nullable = false)
    @Builder.Default
    private boolean perdidaAceite = false;

    @Column(name = "luces_danadas", nullable = false)
    @Builder.Default
    private boolean lucesDanadas = false;

    @Column(name = "espejos_danados", nullable = false)
    @Builder.Default
    private boolean espejosDanados = false;

    @Column(name = "accesorios_faltantes", nullable = false)
    @Builder.Default
    private boolean accesoriosFaltantes = false;

    @Column(name = "nivel_combustible")
    private String nivelCombustible;

    private Integer kilometraje;

    @Column(name = "observaciones_estado")
    private String observacionesEstado;
}
