-- Stock por sector + transferencias entre sectores (schema operaciones)

CREATE SCHEMA IF NOT EXISTS operaciones;

CREATE TABLE IF NOT EXISTS operaciones.stock_producto_sector (
    id_stock        BIGSERIAL PRIMARY KEY,
    id_producto     BIGINT NOT NULL REFERENCES inventario.producto (id_producto),
    id_sector       BIGINT NOT NULL REFERENCES sectores.sector (id_sector),
    cantidad        NUMERIC(15, 2) NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_producto_sector UNIQUE (id_producto, id_sector),
    CONSTRAINT ck_stock_cantidad_non_negative CHECK (cantidad >= 0)
);

CREATE INDEX IF NOT EXISTS idx_stock_producto ON operaciones.stock_producto_sector (id_producto);
CREATE INDEX IF NOT EXISTS idx_stock_sector ON operaciones.stock_producto_sector (id_sector);

CREATE TABLE IF NOT EXISTS operaciones.transferencia (
    id_transferencia    BIGSERIAL PRIMARY KEY,
    numero              VARCHAR(50) NOT NULL,
    id_sector_origen    BIGINT NOT NULL REFERENCES sectores.sector (id_sector),
    id_sector_destino   BIGINT NOT NULL REFERENCES sectores.sector (id_sector),
    observacion         VARCHAR(500),
    estado              VARCHAR(30) NOT NULL DEFAULT 'COMPLETADA',
    fecha               TIMESTAMP NOT NULL DEFAULT NOW(),
    id_persona          BIGINT REFERENCES personas.persona (id_persona),
    CONSTRAINT ck_transferencia_sectores_distintos CHECK (id_sector_origen <> id_sector_destino)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_transferencia_numero ON operaciones.transferencia (numero);
CREATE INDEX IF NOT EXISTS idx_transferencia_origen ON operaciones.transferencia (id_sector_origen);
CREATE INDEX IF NOT EXISTS idx_transferencia_destino ON operaciones.transferencia (id_sector_destino);
CREATE INDEX IF NOT EXISTS idx_transferencia_fecha ON operaciones.transferencia (fecha DESC);

CREATE TABLE IF NOT EXISTS operaciones.transferencia_detalle (
    id_detalle          BIGSERIAL PRIMARY KEY,
    id_transferencia    BIGINT NOT NULL REFERENCES operaciones.transferencia (id_transferencia) ON DELETE CASCADE,
    id_producto         BIGINT NOT NULL REFERENCES inventario.producto (id_producto),
    cantidad            NUMERIC(15, 2) NOT NULL,
    CONSTRAINT ck_detalle_cantidad_positiva CHECK (cantidad > 0)
);

CREATE INDEX IF NOT EXISTS idx_transferencia_detalle_cab ON operaciones.transferencia_detalle (id_transferencia);

-- Migrar stock global actual al primer sector disponible
DO $$
DECLARE
    v_sector_id BIGINT;
BEGIN
    SELECT id_sector INTO v_sector_id
    FROM sectores.sector
    ORDER BY id_sector
    LIMIT 1;

    IF v_sector_id IS NOT NULL THEN
        INSERT INTO operaciones.stock_producto_sector (id_producto, id_sector, cantidad)
        SELECT p.id_producto, v_sector_id, COALESCE(p.stock, 0)
        FROM inventario.producto p
        WHERE NOT EXISTS (
            SELECT 1
            FROM operaciones.stock_producto_sector s
            WHERE s.id_producto = p.id_producto
              AND s.id_sector = v_sector_id
        );
    END IF;
END $$;
