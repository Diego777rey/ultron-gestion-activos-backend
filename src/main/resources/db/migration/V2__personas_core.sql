CREATE TABLE personas.persona (
    id_persona BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(255),
    apellido     VARCHAR(255),
    documento    VARCHAR(255),
    email        VARCHAR(255),
    telefono     VARCHAR(255),
    estado       VARCHAR(255)
);

CREATE TABLE personas.empresa (
    id_empresa     BIGSERIAL PRIMARY KEY,
    razon_social   VARCHAR(255),
    ruc            VARCHAR(255),
    direccion      VARCHAR(255),
    fecha_creacion DATE
);

CREATE TABLE personas.cargo (
    id_cargo            BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255),
    descripcion         VARCHAR(255),
    nivel_autorizacion  INTEGER
);

CREATE TABLE personas.asignacion_cargo (
    id_asignacion BIGSERIAL PRIMARY KEY,
    id_persona    BIGINT NOT NULL REFERENCES personas.persona (id_persona),
    id_cargo      BIGINT NOT NULL REFERENCES personas.cargo (id_cargo),
    id_empresa    BIGINT NOT NULL REFERENCES personas.empresa (id_empresa),
    fecha_inicio  DATE,
    fecha_fin     DATE,
    activo        BOOLEAN
);

CREATE TABLE personas.propiedad_accionaria (
    id_propiedad         BIGSERIAL PRIMARY KEY,
    id_persona           BIGINT NOT NULL REFERENCES personas.persona (id_persona),
    id_empresa           BIGINT NOT NULL REFERENCES personas.empresa (id_empresa),
    porcentaje_acciones  NUMERIC(19, 2),
    fecha_inicio         DATE,
    fecha_fin            DATE
);
