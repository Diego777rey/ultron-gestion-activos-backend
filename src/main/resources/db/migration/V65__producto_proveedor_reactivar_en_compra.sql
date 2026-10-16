-- Al guardar una compra el producto queda asociado al proveedor,
-- también si la asociación existía y estaba dada de baja.
CREATE OR REPLACE FUNCTION compras.actualizar_producto_proveedor()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO compras.producto_proveedor (id_producto, id_proveedor, precio_ultima_compra, fecha_ultima_compra, activo)
    SELECT
        NEW.id_producto,
        c.id_proveedor,
        NEW.precio_costo,
        c.fecha_compra,
        true
    FROM compras.compra c
    WHERE c.id_compra = NEW.id_compra
    ON CONFLICT (id_producto, id_proveedor)
    DO UPDATE SET
        precio_ultima_compra = EXCLUDED.precio_ultima_compra,
        fecha_ultima_compra = EXCLUDED.fecha_ultima_compra,
        activo = true;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
