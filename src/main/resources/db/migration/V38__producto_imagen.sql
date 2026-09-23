-- Agregar columna imagen a la tabla producto
ALTER TABLE inventario.producto ADD COLUMN imagen VARCHAR(500);

-- Comentario de la columna
COMMENT ON COLUMN inventario.producto.imagen IS 'Ruta relativa de la imagen del producto almacenada en el servidor';
