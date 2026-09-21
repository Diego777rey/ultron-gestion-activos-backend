ALTER TABLE operaciones.transferencia
    ADD COLUMN IF NOT EXISTS id_persona_recepcion BIGINT REFERENCES personas.persona (id_persona);

CREATE INDEX IF NOT EXISTS idx_transferencia_persona_recepcion
    ON operaciones.transferencia (id_persona_recepcion);
