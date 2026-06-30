package com.dev.ultron.dto.personas.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioOutput implements Serializable {
    private Long id;
    private String username;
    private String email;
    private Boolean activo;
    private Long id_funcionario;
    private FuncionarioOutput funcionario;
    private List<RoleOutput> roles;
}
