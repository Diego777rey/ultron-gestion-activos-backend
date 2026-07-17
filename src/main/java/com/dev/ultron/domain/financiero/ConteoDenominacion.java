package com.dev.ultron.domain.financiero;

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
@Table(name = "conteo_denominacion", schema = "financiero")
public class ConteoDenominacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_conteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion_caja", nullable = false)
    private SesionCaja sesionCaja;

    private String tipo;
    private String moneda;
    private BigDecimal valorDenominacion;
    private Integer cantidad;
}
