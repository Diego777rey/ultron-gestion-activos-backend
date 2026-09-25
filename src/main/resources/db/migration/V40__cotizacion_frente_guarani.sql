COMMENT ON TABLE financiero.cotizacion IS 'Cotizaciones de monedas extranjeras expresadas en guaraníes';
COMMENT ON COLUMN financiero.cotizacion.moneda IS 'Moneda cotizada frente al guaraní: DOLAR o REAL';
COMMENT ON COLUMN financiero.cotizacion.valor IS 'Guaraníes equivalentes a 1 unidad de la moneda';

ALTER TABLE financiero.cotizacion
    DROP CONSTRAINT IF EXISTS chk_cotizacion_moneda;

ALTER TABLE financiero.cotizacion
    ADD CONSTRAINT chk_cotizacion_moneda CHECK (moneda IN ('DOLAR', 'REAL'));
