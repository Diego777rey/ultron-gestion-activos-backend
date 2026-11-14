package com.dev.ultron.domain.web;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.dev.ultron.domain.personas.Cliente;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cuenta_cliente", schema = "web")
public class CuentaClienteWeb implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cuenta_cliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false, unique = true)
    private Cliente cliente;

    private String email;
    private String passwordHash;
    private boolean activo;
    private OffsetDateTime creadoEn;

    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        activo = true;
    }
}
