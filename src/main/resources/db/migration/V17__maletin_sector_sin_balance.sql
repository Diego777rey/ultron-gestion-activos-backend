-- Maletín como sobre físico (flujo general6): código + sector, sin balances.
-- El dinero se controla en la sesión de caja / movimientos, no en el maletín.

ALTER TABLE financiero.maletin
    ADD COLUMN IF NOT EXISTS abierto BOOLEAN;

UPDATE financiero.maletin
SET abierto = CASE WHEN UPPER(COALESCE(estado, 'CERRADO')) = 'ABIERTO' THEN TRUE ELSE FALSE END
WHERE abierto IS NULL;

ALTER TABLE financiero.maletin
    ALTER COLUMN abierto SET DEFAULT FALSE;

ALTER TABLE financiero.maletin
    ALTER COLUMN abierto SET NOT NULL;

ALTER TABLE financiero.maletin
    DROP COLUMN IF EXISTS estado;

ALTER TABLE financiero.maletin
    DROP COLUMN IF EXISTS balance_pyg;

ALTER TABLE financiero.maletin
    DROP COLUMN IF EXISTS balance_usd;

ALTER TABLE financiero.maletin
    DROP COLUMN IF EXISTS balance_brl;

ALTER TABLE financiero.maletin
    ADD COLUMN IF NOT EXISTS id_sector BIGINT;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM financiero.maletin WHERE id_sector IS NULL) THEN
        IF NOT EXISTS (SELECT 1 FROM sectores.sector) THEN
            INSERT INTO sectores.sector (nombre, descripcion, estado)
            VALUES ('GENERAL', 'Sector creado automáticamente para asociar maletines existentes', TRUE);
        END IF;

        UPDATE financiero.maletin
        SET id_sector = (SELECT id_sector FROM sectores.sector ORDER BY id_sector LIMIT 1)
        WHERE id_sector IS NULL;
    END IF;
END $$;

ALTER TABLE financiero.maletin
    ALTER COLUMN id_sector SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_maletin_sector'
    ) THEN
        ALTER TABLE financiero.maletin
            ADD CONSTRAINT fk_maletin_sector
            FOREIGN KEY (id_sector) REFERENCES sectores.sector (id_sector);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_maletin_sector ON financiero.maletin (id_sector);
CREATE INDEX IF NOT EXISTS idx_maletin_abierto ON financiero.maletin (abierto);
