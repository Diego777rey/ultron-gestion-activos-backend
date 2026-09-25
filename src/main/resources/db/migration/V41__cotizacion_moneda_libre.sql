ALTER TABLE financiero.cotizacion
    DROP CONSTRAINT IF EXISTS chk_cotizacion_moneda;

ALTER TABLE financiero.cotizacion
    ALTER COLUMN moneda TYPE VARCHAR(80);

COMMENT ON COLUMN financiero.cotizacion.moneda IS 'Nombre de la moneda cotizada frente al guaraní';
