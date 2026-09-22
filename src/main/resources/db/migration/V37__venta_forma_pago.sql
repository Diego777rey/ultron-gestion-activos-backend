-- =========================================================
-- V37: Agregar forma de pago a ventas
-- =========================================================

ALTER TABLE financiero.venta
ADD COLUMN forma_pago VARCHAR(20) DEFAULT 'EFECTIVO';

COMMENT ON COLUMN financiero.venta.forma_pago IS 'Forma de pago: EFECTIVO, TARJETA, TRANSFERENCIA';

-- Actualizar ventas existentes con valor por defecto
UPDATE financiero.venta SET forma_pago = 'EFECTIVO' WHERE forma_pago IS NULL;
