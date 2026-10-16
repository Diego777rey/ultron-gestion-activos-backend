package com.dev.ultron.domain.compras;

import com.dev.ultron.domain.sectores.Sector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compra", schema = "compras")
public class Compra implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor")
    private Vendedor vendedor;

    /** Lugar físico donde entra la mercadería al stock. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector")
    private Sector sector;

    @Column(name = "numero_factura", length = 100)
    private String numero_factura;

    @Column(length = 100)
    private String timbrado;

    @Column(name = "fecha_factura")
    private LocalDate fecha_factura;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fecha_compra;

    @Column(name = "forma_pago", nullable = false, length = 50)
    private String forma_pago;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String moneda = "PYG";

    @Column(precision = 15, scale = 4)
    @Builder.Default
    private BigDecimal cotizacion = BigDecimal.ONE;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "total_iva", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal total_iva = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoCompra estado = EstadoCompra.BORRADOR;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_registro")
    private LocalDateTime fecha_registro;

    @Column(name = "fecha_ultima_modificacion")
    private LocalDateTime fecha_ultima_modificacion;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id_item_compra ASC")
    @Builder.Default
    private List<ItemCompra> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fecha_registro = LocalDateTime.now();
        if (fecha_compra == null) {
            fecha_compra = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fecha_ultima_modificacion = LocalDateTime.now();
    }

    public enum EstadoCompra {
        BORRADOR,
        CONFIRMADA,
        RECIBIDA,
        CANCELADA
    }
}
