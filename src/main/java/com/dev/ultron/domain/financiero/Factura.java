package com.dev.ultron.domain.financiero;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.domain.personas.Empresa;
import com.dev.ultron.domain.personas.Usuario;
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
@Table(name = "factura", schema = "financiero")
public class Factura implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_factura;

    private String numero_factura;
    private String timbrado;
    private LocalDateTime fecha_emision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_timbrado", nullable = false)
    private Timbrado timbradoEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion_caja")
    private SesionCaja sesionCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    private String cliente_nombre;
    private String cliente_documento;
    private String cliente_ruc;
    private String cliente_direccion;

    private BigDecimal subtotal;
    private BigDecimal total_iva_5;
    private BigDecimal total_iva_10;
    private BigDecimal total_exenta;
    private BigDecimal total_iva;
    private BigDecimal total;

    private String forma_pago;
    private String moneda;

    private String estado;
    private LocalDateTime fecha_anulacion;
    private String motivo_anulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_anulacion")
    private Usuario usuario_anulacion;

    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_emisor", nullable = false)
    private Usuario usuario_emisor;

    private LocalDateTime fecha_creacion;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numero_linea ASC")
    @Builder.Default
    private List<DetalleFactura> detalles = new ArrayList<>();
}
