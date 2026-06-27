package com.dev.ultron.domain.personas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RolePermisoId implements Serializable {

    @Column(name = "role_id")
    private Long role_id;

    @Column(name = "permiso_id")
    private Long permiso_id;
}
