package com.dev.ultron.domain.patrimonio;

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
@Table(name = "mueble", schema = "patrimonio")
@PrimaryKeyJoinColumn(name = "id_mueble")
public class Mueble extends Bien {

    private String tipo_mueble;
    private String ubicacion;
    private Integer cantidad;
}
