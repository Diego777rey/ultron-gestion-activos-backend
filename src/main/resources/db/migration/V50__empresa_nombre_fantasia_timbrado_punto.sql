-- Nombre de fantasía: se imprime en la factura junto a la razón social.
ALTER TABLE personas.empresa
    ADD COLUMN IF NOT EXISTS nombre_fantasia VARCHAR(255);

COMMENT ON COLUMN personas.empresa.nombre_fantasia IS 'Nombre comercial que se imprime en la factura, además de la razón social';

ALTER TABLE personas.empresa
    ALTER COLUMN direccion TYPE VARCHAR(500);

ALTER TABLE personas.empresa
    ALTER COLUMN actividad_economica TYPE VARCHAR(500);

-- Un mismo número de timbrado puede autorizar más de un punto de expedición.
ALTER TABLE financiero.timbrado DROP CONSTRAINT IF EXISTS uq_timbrado_numero;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT uq_timbrado_punto
        UNIQUE (numero_timbrado, establecimiento, punto_expedicion);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;
