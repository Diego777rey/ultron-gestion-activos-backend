-- Asociar cada caja a un sector (ubicación física)

ALTER TABLE financiero.caja
    ADD COLUMN IF NOT EXISTS id_sector BIGINT;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM financiero.caja WHERE id_sector IS NULL) THEN
        IF NOT EXISTS (SELECT 1 FROM sectores.sector) THEN
            INSERT INTO sectores.sector (nombre, descripcion, estado)
            VALUES ('GENERAL', 'Sector creado automáticamente para asociar cajas existentes', TRUE);
        END IF;

        UPDATE financiero.caja
        SET id_sector = (SELECT id_sector FROM sectores.sector ORDER BY id_sector LIMIT 1)
        WHERE id_sector IS NULL;
    END IF;
END $$;

ALTER TABLE financiero.caja
    ALTER COLUMN id_sector SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_caja_sector'
    ) THEN
        ALTER TABLE financiero.caja
            ADD CONSTRAINT fk_caja_sector
            FOREIGN KEY (id_sector) REFERENCES sectores.sector (id_sector);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_caja_sector ON financiero.caja (id_sector);
