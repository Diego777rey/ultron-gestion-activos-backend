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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caja", schema = "financiero")
public class Caja implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_caja;

    private String nombre;
    private BigDecimal saldo_actual;
    private Long id_empresa;

    @ManyToOne
    @JoinColumn(name = "id_responsable")
    private Persona responsable;

    private Boolean activa;
}
