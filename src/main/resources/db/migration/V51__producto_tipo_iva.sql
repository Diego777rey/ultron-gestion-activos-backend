ALTER TABLE inventario.producto
    ADD COLUMN IF NOT EXISTS tipo_iva VARCHAR(10) NOT NULL DEFAULT '10';

COMMENT ON COLUMN inventario.producto.tipo_iva IS 'Tratamiento de IVA del producto: 10, 5 o EXENTA';

UPDATE inventario.producto
SET tipo_iva = '10'
WHERE tipo_iva IS NULL OR tipo_iva NOT IN ('10', '5', 'EXENTA');
