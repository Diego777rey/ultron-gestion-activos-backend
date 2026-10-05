-- =========================================================
-- V54: El vuelto puede entregarse en cualquier moneda
-- =========================================================
-- vuelto pasa a estar expresado en moneda_vuelto; vuelto_pyg guarda su equivalente en guaraníes.

ALTER TABLE financiero.venta
    ADD COLUMN IF NOT EXISTS moneda_vuelto VARCHAR(80),
    ADD COLUMN IF NOT EXISTS vuelto_pyg NUMERIC(19, 2);

-- Las ventas anteriores a esta versión devolvieron siempre en guaraníes.
UPDATE financiero.venta
SET moneda_vuelto = 'PYG',
    vuelto_pyg = vuelto
WHERE vuelto IS NOT NULL AND vuelto_pyg IS NULL;

COMMENT ON COLUMN financiero.venta.moneda_vuelto IS 'Moneda en la que se entregó el vuelto';
COMMENT ON COLUMN financiero.venta.vuelto IS 'Vuelto entregado al cliente, en moneda_vuelto. NULL si no se informó el monto recibido';
COMMENT ON COLUMN financiero.venta.vuelto_pyg IS 'Equivalente en guaraníes del vuelto entregado';
