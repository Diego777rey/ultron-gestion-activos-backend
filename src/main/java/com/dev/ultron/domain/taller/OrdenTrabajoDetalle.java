package com.dev.ultron.domain.taller;

import com.dev.ultron.domain.inventario.Producto;
import com.dev.ultron.domain.inventario.Servicio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_trabajo_detalle", schema = "taller")
public class OrdenTrabajoDetalle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @Column(nullable = false)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "etapa_origen", nullable = false)
    private String etapaOrigen;

    @PrePersist
    protected void onCreate() {
        if (cantidad == null) {
            cantidad = BigDecimal.ONE;
        }
        if (precioUnitario == null) {
            precioUnitario = BigDecimal.ZERO;
        }
        if (subtotal == null) {
            subtotal = cantidad.multiply(precioUnitario);
        }
        if (etapaOrigen == null) {
            etapaOrigen = "DIAGNOSTICO";
        }
    }
}
