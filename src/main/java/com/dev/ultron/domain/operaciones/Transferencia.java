package com.dev.ultron.domain.operaciones;

import com.dev.ultron.domain.personas.Persona;
import com.dev.ultron.domain.sectores.Sector;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transferencia", schema = "operaciones")
public class Transferencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_transferencia;

    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector_origen", nullable = false)
    private Sector sectorOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sector_destino", nullable = false)
    private Sector sectorDestino;

    private String observacion;
    private String estado;
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @Builder.Default
    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id_detalle ASC")
    private List<TransferenciaDetalle> detalles = new ArrayList<>();
}
