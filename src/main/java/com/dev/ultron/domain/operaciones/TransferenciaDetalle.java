package com.dev.ultron.domain.operaciones;

import com.dev.ultron.domain.inventario.PresentacionProducto;
import com.dev.ultron.domain.inventario.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "transferencia_detalle", schema = "operaciones")
public class TransferenciaDetalle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia", nullable = false)
    private Transferencia transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presentacion_producto")
    private PresentacionProducto presentacionProducto;

    private BigDecimal cantidad;

    /** PENDIENTE | VERIFICADO | RECHAZADO */
    @Builder.Default
    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    /** AVERIADO | VENCIDO | ENVIADO_MAL | OTRO */
    @Column(name = "motivo_rechazo", length = 30)
    private String motivoRechazo;

    @Column(name = "motivo_rechazo_detalle", length = 255)
    private String motivoRechazoDetalle;
}
