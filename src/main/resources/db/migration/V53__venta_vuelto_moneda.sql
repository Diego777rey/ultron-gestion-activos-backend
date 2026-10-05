-- =========================================================
-- V53: Vuelto cuando el cliente paga en moneda extranjera
-- =========================================================
-- monto_recibido pasa a estar expresado en la moneda de la venta (columna `moneda`).
-- monto_recibido_pyg guarda su equivalente en guaraníes; el vuelto siempre es en guaraníes.

ALTER TABLE financiero.venta
    ADD COLUMN IF NOT EXISTS monto_recibido_pyg NUMERIC(19, 2);

UPDATE financiero.venta
SET monto_recibido_pyg = monto_recibido
WHERE monto_recibido IS NOT NULL AND monto_recibido_pyg IS NULL;

COMMENT ON COLUMN financiero.venta.monto_recibido IS 'Efectivo que entregó el cliente, en la moneda de la venta (columna moneda). NULL si no se informó';
COMMENT ON COLUMN financiero.venta.monto_recibido_pyg IS 'Equivalente en guaraníes del monto recibido, según la cotización usada en la venta';
COMMENT ON COLUMN financiero.venta.vuelto IS 'Vuelto entregado al cliente, siempre en guaraníes. NULL si no se informó el monto recibido';
