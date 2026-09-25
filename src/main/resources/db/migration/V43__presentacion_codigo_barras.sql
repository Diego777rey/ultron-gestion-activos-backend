-- Código de barras propio de cada presentación (unidad, pack, caja).

ALTER TABLE inventario.presentacion_producto
    ADD COLUMN codigo_barras VARCHAR(100) NOT NULL DEFAULT '';

ALTER TABLE inventario.presentacion_producto
    ALTER COLUMN codigo_barras DROP DEFAULT;

CREATE UNIQUE INDEX ux_presentacion_producto_codigo_barras
    ON inventario.presentacion_producto (codigo_barras)
    WHERE codigo_barras IS NOT NULL AND TRIM(codigo_barras) <> '';
