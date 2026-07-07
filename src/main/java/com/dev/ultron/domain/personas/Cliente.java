package com.dev.ultron.domain.personas;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

import com.dev.ultron.domain.patrimonio.Vehiculo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cliente", schema = "personas")
public class Cliente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false, unique = true)
    private Persona persona;

    private String ruc;
    private String tipoCliente;
    private BigDecimal limiteCredito;
    private LocalDate fechaRegistro;
    private String observaciones;
    private boolean estado;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Vehiculo> vehiculos;
}
