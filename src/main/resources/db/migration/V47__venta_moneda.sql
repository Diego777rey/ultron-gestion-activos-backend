ALTER TABLE financiero.venta
    ADD COLUMN IF NOT EXISTS moneda VARCHAR(80) DEFAULT 'PYG',
    ADD COLUMN IF NOT EXISTS monto_moneda_original NUMERIC(19, 2);

COMMENT ON COLUMN financiero.venta.moneda IS 'Moneda en la que se cobró la venta. PYG si el cobro fue en guaraníes';
COMMENT ON COLUMN financiero.venta.monto_moneda_original IS 'Importe en la moneda original cuando el cobro no fue en guaraníes';

UPDATE financiero.venta SET moneda = 'PYG' WHERE moneda IS NULL;
