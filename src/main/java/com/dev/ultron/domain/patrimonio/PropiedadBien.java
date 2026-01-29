package com.dev.ultron.domain.patrimonio;

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
@Table(name = "propiedad_bien", schema = "patrimonio")
public class PropiedadBien implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_propiedad_bien;

    @ManyToOne
    @JoinColumn(name = "id_bien")
    private Bien bien;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    private BigDecimal porcentaje_propiedad;
}
