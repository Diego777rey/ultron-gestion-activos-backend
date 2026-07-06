package com.dev.ultron.domain.patrimonio;

import com.dev.ultron.domain.personas.Cliente;
import com.dev.ultron.utilitarios.UppercaseEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vehiculo", schema = "patrimonio")
@PrimaryKeyJoinColumn(name = "id_vehiculo")
@EntityListeners(UppercaseEntityListener.class)
public class Vehiculo extends Bien {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    private String marca;
    private String modelo;
    private Integer anio;
    private String chapa;

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;
}
