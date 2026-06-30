package com.dev.ultron.domain.personas;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dev.ultron.utilitarios.UppercaseEntityListener;
import jakarta.persistence.EntityListeners;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario", schema = "personas")
@EntityListeners(UppercaseEntityListener.class)
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", unique = true)
    private Funcionario funcionario;

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private List<UsuarioRole> usuarioRoles = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private List<ResetearContrasenha> reseteosContrasenha = new ArrayList<>();
}
