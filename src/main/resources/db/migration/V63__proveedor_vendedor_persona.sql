-- Proveedor y vendedor comparten la persona del sistema
-- (la misma que usa un funcionario o un cliente).

ALTER TABLE compras.proveedor
    ADD COLUMN id_persona BIGINT REFERENCES personas.persona (id_persona);

CREATE UNIQUE INDEX uq_proveedor_id_persona
    ON compras.proveedor (id_persona)
    WHERE id_persona IS NOT NULL;

UPDATE compras.proveedor p
SET id_persona = per.id_persona
FROM personas.persona per
WHERE p.id_persona IS NULL
  AND per.documento = regexp_replace(regexp_replace(upper(p.ruc), '[.\s]', '', 'g'), '-\d$', '');

ALTER TABLE compras.vendedor
    ADD COLUMN documento VARCHAR(50);

ALTER TABLE compras.vendedor
    ADD COLUMN id_persona BIGINT REFERENCES personas.persona (id_persona);

CREATE INDEX idx_vendedor_persona ON compras.vendedor (id_persona);

COMMENT ON COLUMN compras.proveedor.id_persona IS 'Persona del sistema. Un funcionario puede ser también proveedor.';
COMMENT ON COLUMN compras.vendedor.id_persona IS 'Persona del sistema, cuando el vendedor ya está registrado por su CI.';
