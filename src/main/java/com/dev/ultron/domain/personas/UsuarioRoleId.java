package com.dev.ultron.domain.personas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UsuarioRoleId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Column(name = "role_id")
    private Long role_id;
}
