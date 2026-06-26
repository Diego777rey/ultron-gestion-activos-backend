CREATE TABLE patrimonio.bien (
    id_bien            BIGSERIAL PRIMARY KEY,
    tipo               VARCHAR(255),
    descripcion        VARCHAR(255),
    valor              NUMERIC(19, 2),
    fecha_adquisicion  DATE,
    id_empresa         BIGINT,
    estado             VARCHAR(255)
);

CREATE TABLE patrimonio.vehiculo (
    id_vehiculo   BIGINT PRIMARY KEY REFERENCES patrimonio.bien (id_bien),
    marca         VARCHAR(255),
    modelo        VARCHAR(255),
    anio          INTEGER,
    chapa         VARCHAR(255),
    tipo_vehiculo VARCHAR(255)
);

CREATE TABLE patrimonio.mueble (
    id_mueble   BIGINT PRIMARY KEY REFERENCES patrimonio.bien (id_bien),
    tipo_mueble VARCHAR(255),
    ubicacion   VARCHAR(255),
    cantidad    INTEGER
);

CREATE TABLE patrimonio.propiedad_bien (
    id_propiedad_bien   BIGSERIAL PRIMARY KEY,
    id_bien             BIGINT NOT NULL REFERENCES patrimonio.bien (id_bien),
    id_persona          BIGINT NOT NULL REFERENCES personas.persona (id_persona),
    porcentaje_propiedad NUMERIC(19, 2)
);
