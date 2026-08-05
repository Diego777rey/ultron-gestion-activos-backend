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
@Table(name = "orden_recepcion", schema = "taller")
public class OrdenRecepcion implements Serializable {

    @Id
    @Column(name = "id_orden_trabajo")
    private Long idOrdenTrabajo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_orden_trabajo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "descripcion_falla")
    private String descripcionFalla;
}
