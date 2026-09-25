package com.dev.ultron.domain.inventario;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "producto", schema = "inventario")
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String codigoBarras;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private String ubicacion;
    private boolean estado;
    private String imagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_producto", nullable = false)
    private CategoriaProducto categoriaProducto;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @BatchSize(size = 32)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC, id_presentacion_producto ASC")
    private List<PresentacionProducto> presentaciones = new ArrayList<>();
}
