package com.dev.ultron.domain.financiero;

import com.dev.ultron.domain.personas.Persona;
import jakarta.persistence.Entity;
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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimiento_caja", schema = "financiero")
public class MovimientoCaja implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_movimiento;

    @ManyToOne
    @JoinColumn(name = "id_caja")
    private Caja caja;

    private String tipo;
    private BigDecimal monto;
    private String concepto;
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "id_autorizador")
    private Persona autorizador;

    private String referencia;
    private String moneda;

    @ManyToOne
    @JoinColumn(name = "id_maletin")
    private Maletin maletin;

    @ManyToOne
    @JoinColumn(name = "id_sesion_caja")
    private SesionCaja sesionCaja;
}
