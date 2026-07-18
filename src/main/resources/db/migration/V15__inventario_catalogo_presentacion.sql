-- =============================================================
-- Catálogo de Presentaciones
-- =============================================================
-- Tipos de presentación que el usuario registra (ej: UNIDAD, PACK X 12).
-- Luego se asocian a productos vía inventario.presentacion_producto.

CREATE TABLE IF NOT EXISTS inventario.presentacion (
    id_presentacion BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    cantidad DECIMAL(15,2) NOT NULL DEFAULT 1,
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_presentacion_nombre
    ON inventario.presentacion (nombre);

-- El tipo de presentacion_producto deja de tener default hardcodeado.
ALTER TABLE inventario.presentacion_producto
    ALTER COLUMN tipo DROP DEFAULT;
