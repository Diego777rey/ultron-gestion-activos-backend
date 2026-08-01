-- V26: Eliminar presentaciones y consolidar precio/código de barras en producto

-- 1. Agregar código de barras al producto
ALTER TABLE inventario.producto
    ADD COLUMN IF NOT EXISTS codigo_barras VARCHAR(100);

-- 2. Migrar precio y código de barras desde la presentación principal (o la primera)
UPDATE inventario.producto p
SET
    precio_venta = COALESCE(
        (
            SELECT pp.precio
            FROM inventario.presentacion_producto pp
            WHERE pp.id_producto = p.id_producto
              AND pp.estado IS DISTINCT FROM FALSE
            ORDER BY pp.principal DESC NULLS LAST, pp.id_presentacion_producto ASC
            LIMIT 1
        ),
        p.precio_venta
    ),
    codigo_barras = COALESCE(
        p.codigo_barras,
        (
            SELECT pp.codigo_barras
            FROM inventario.presentacion_producto pp
            WHERE pp.id_producto = p.id_producto
              AND pp.codigo_barras IS NOT NULL
              AND TRIM(pp.codigo_barras) <> ''
            ORDER BY pp.principal DESC NULLS LAST, pp.id_presentacion_producto ASC
            LIMIT 1
        )
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_producto_codigo_barras
    ON inventario.producto (codigo_barras)
    WHERE codigo_barras IS NOT NULL AND TRIM(codigo_barras) <> '';

-- 3. Quitar columnas/FK de presentaciones en transferencias y ventas
ALTER TABLE operaciones.transferencia_detalle
    DROP COLUMN IF EXISTS id_presentacion_producto CASCADE;

ALTER TABLE financiero.detalle_venta
    DROP COLUMN IF EXISTS id_presentacion CASCADE;

-- 4. Eliminar tablas de presentaciones
DROP TABLE IF EXISTS inventario.presentacion_producto CASCADE;
DROP TABLE IF EXISTS inventario.presentacion CASCADE;
