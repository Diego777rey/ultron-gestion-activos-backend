-- Tabla funcionario: extiende persona mediante FK
CREATE TABLE personas.funcionario (
    id_funcionario BIGSERIAL PRIMARY KEY,
    id_persona     BIGINT NOT NULL UNIQUE REFERENCES personas.persona (id_persona),
    sueldo         NUMERIC(19, 2),
    sector         VARCHAR(255),
    fecha_ingreso  DATE,
    face_prueba    BOOLEAN DEFAULT FALSE,
    estado         BOOLEAN DEFAULT TRUE
);

-- Tabla cliente: extiende persona mediante FK
CREATE TABLE personas.cliente (
    id_cliente        BIGSERIAL PRIMARY KEY,
    id_persona        BIGINT NOT NULL UNIQUE REFERENCES personas.persona (id_persona),
    ruc               VARCHAR(50),
    tipo_cliente      VARCHAR(50),
    limite_credito    NUMERIC(19, 2),
    fecha_registro    DATE,
    observaciones     VARCHAR(500),
    estado            BOOLEAN DEFAULT TRUE
);
