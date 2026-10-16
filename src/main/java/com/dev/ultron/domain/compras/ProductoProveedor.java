package com.dev.ultron.domain.compras;

import com.dev.ultron.domain.inventario.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto_proveedor", schema = "compras",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_producto", "id_proveedor"}))
public class ProductoProveedor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto_proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "precio_ultima_compra", precision = 15, scale = 2)
    private BigDecimal precio_ultima_compra;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fecha_ultima_compra;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fecha_registro;

    @PrePersist
    protected void onCreate() {
        fecha_registro = LocalDateTime.now();
    }
}
