-- Visibilidad del producto en la tienda web (Red Automotriz).
-- Solo se publica si mostrar_en_web = true, hay imagen y precio de venta > 0.
ALTER TABLE inventario.producto
    ADD COLUMN mostrar_en_web BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN inventario.producto.mostrar_en_web IS
    'Si true, el producto puede mostrarse en la página web (requiere imagen y precio)';
