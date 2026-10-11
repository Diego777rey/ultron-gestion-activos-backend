package com.dev.ultron.domain.compras;

import com.dev.ultron.domain.inventario.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_compra", schema = "compras")
public class ItemCompra implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_item_compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_costo", nullable = false, precision = 15, scale = 2)
    private BigDecimal precio_costo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tipo_iva", length = 20)
    private String tipo_iva;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(name = "fecha_vencimiento")
    private LocalDate fecha_vencimiento;

    @Column(length = 100)
    private String lote;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_registro")
    private LocalDateTime fecha_registro;

    @PrePersist
    protected void onCreate() {
        fecha_registro = LocalDateTime.now();
    }
}
