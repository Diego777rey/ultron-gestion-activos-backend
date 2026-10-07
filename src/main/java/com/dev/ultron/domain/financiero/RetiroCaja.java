package com.dev.ultron.domain.financiero;

import com.dev.ultron.domain.personas.Usuario;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Efectivo que se saca de la caja durante una sesión abierta. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "retiro_caja", schema = "financiero")
public class RetiroCaja implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_retiro_caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion_caja", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SesionCaja sesionCaja;

    /** Código ISO: PYG, BRL o USD. */
    private String moneda;
    private BigDecimal monto;
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsable", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario responsable;

    /** Usuario logueado que cargó el retiro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_registro")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario registradoPor;

    private LocalDateTime fecha;
}
