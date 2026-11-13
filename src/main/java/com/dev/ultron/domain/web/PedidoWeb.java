package com.dev.ultron.domain.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
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
@Table(name = "pedido_web", schema = "web")
public class PedidoWeb implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_pedido_web;

    private String codigo;
    private String estado;
    private String formaPago;
    private String moneda;
    private BigDecimal total;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String direccionEnvio;
    private String ciudad;
    private String notas;
    private String pagoTransaccionId;
    private String pagoUltimosCuatro;
    private String pagoMensaje;
    private OffsetDateTime creadoEn;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @BatchSize(size = 32)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id_detalle_pedido_web ASC")
    private List<DetallePedidoWeb> detalles = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
        }
        if (moneda == null || moneda.isBlank()) {
            moneda = "PYG";
        }
    }
}
