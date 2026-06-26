CREATE TABLE personas.usuario (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(255),
    password     VARCHAR(255),
    email        VARCHAR(255),
    activo       BOOLEAN,
    sucursal_id  BIGINT
);

CREATE TABLE personas.role (
    id           BIGSERIAL PRIMARY KEY,
    descripcion  VARCHAR(255),
    activo       VARCHAR(255)
);

CREATE TABLE personas.permiso (
    id           BIGSERIAL PRIMARY KEY,
    modulo       VARCHAR(255),
    accion       VARCHAR(255),
    descripcion  VARCHAR(255)
);

CREATE TABLE personas.usuario_role (
    usuario_id BIGINT NOT NULL REFERENCES personas.usuario (id),
    role_id    BIGINT NOT NULL REFERENCES personas.role (id),
    PRIMARY KEY (usuario_id, role_id)
);

CREATE TABLE personas.role_permiso (
    role_id    BIGINT NOT NULL REFERENCES personas.role (id),
    permiso_id BIGINT NOT NULL REFERENCES personas.permiso (id),
    PRIMARY KEY (role_id, permiso_id)
);

CREATE TABLE personas.resetear_contrasenha (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255),
    expiracion  TIMESTAMP,
    usado       BOOLEAN,
    usuario_id  BIGINT REFERENCES personas.usuario (id)
);
