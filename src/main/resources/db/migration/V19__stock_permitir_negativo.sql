-- Permitir stock cero o negativo por sector (transferencias y ajustes)

ALTER TABLE operaciones.stock_producto_sector
    DROP CONSTRAINT IF EXISTS ck_stock_cantidad_non_negative;
