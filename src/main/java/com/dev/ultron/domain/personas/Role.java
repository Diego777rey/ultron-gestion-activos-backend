package com.dev.ultron.domain.personas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role", schema = "personas")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private String activo;

    @OneToMany(mappedBy = "role")
    @Builder.Default
    private List<UsuarioRole> usuarioRoles = new ArrayList<>();

    @OneToMany(mappedBy = "role")
    @Builder.Default
    private List<RolePermiso> rolePermisos = new ArrayList<>();
}
