-- =========================================================
-- V52: Monto recibido y vuelto en ventas en efectivo
-- =========================================================

ALTER TABLE financiero.venta
    ADD COLUMN IF NOT EXISTS monto_recibido NUMERIC(19, 2),
    ADD COLUMN IF NOT EXISTS vuelto NUMERIC(19, 2);

COMMENT ON COLUMN financiero.venta.monto_recibido IS 'Efectivo en guaraníes que entregó el cliente. NULL si no se informó (pago exacto, tarjeta o transferencia)';
COMMENT ON COLUMN financiero.venta.vuelto IS 'Vuelto en guaraníes entregado al cliente. NULL si no se informó el monto recibido';
