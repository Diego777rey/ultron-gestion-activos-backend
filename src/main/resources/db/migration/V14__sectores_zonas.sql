CREATE SCHEMA IF NOT EXISTS sectores;

CREATE TABLE IF NOT EXISTS sectores.sector (
    id_sector BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sector_nombre ON sectores.sector (nombre);

CREATE TABLE IF NOT EXISTS sectores.zona (
    id_zona BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    id_sector BIGINT NOT NULL REFERENCES sectores.sector(id_sector)
);

CREATE INDEX IF NOT EXISTS idx_zona_sector ON sectores.zona (id_sector);
CREATE UNIQUE INDEX IF NOT EXISTS uk_zona_nombre_sector ON sectores.zona (id_sector, nombre);
