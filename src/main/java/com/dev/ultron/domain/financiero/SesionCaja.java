package com.dev.ultron.domain.financiero;

import com.dev.ultron.domain.personas.Persona;
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
@Table(name = "sesion_caja", schema = "financiero")
public class SesionCaja implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_sesion_caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maletin", nullable = false)
    private Maletin maletin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    private String estado;
    private BigDecimal montoInicialPyg;
    private BigDecimal montoInicialUsd;
    private BigDecimal montoInicialBrl;
    private BigDecimal montoFinalPyg;
    private BigDecimal montoFinalUsd;
    private BigDecimal montoFinalBrl;
    private BigDecimal diferenciaPyg;
    private BigDecimal diferenciaUsd;
    private BigDecimal diferenciaBrl;
    private BigDecimal totalVentasPyg;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    @OneToMany(mappedBy = "sesionCaja", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id_conteo ASC")
    @Builder.Default
    private List<ConteoDenominacion> conteos = new ArrayList<>();
}
