package com.dev.ultron.domain.patrimonio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Vehiculo extends Bien {

    private String marca;
    private String modelo;
    private Integer anio;
    private String chapa;

    @Column(name = "tipo_vehiculo")
    private String tipo; // Sedan, Truck, etc.
}
