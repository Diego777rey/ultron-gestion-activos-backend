package com.dev.ultron.domain.patrimonio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bien", schema = "patrimonio")
@Inheritance(strategy = InheritanceType.JOINED)
public class Bien implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bien;

    private String tipo; // MUEBLE / VEHICULO / OTRO
    private String descripcion;
    private BigDecimal valor;
    private LocalDate fecha_adquisicion;
    private Long id_empresa;
    private String estado;
}
