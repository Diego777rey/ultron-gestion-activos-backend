-- Presentaciones comerciales de un producto.
-- La descripción la carga el usuario (Unidad, Pack de 6, Caja de 12 unidades).
-- Cada presentación tiene su cantidad de unidades y su precio de venta.

CREATE TABLE inventario.presentacion_producto (
    id_presentacion_producto BIGSERIAL PRIMARY KEY,
    id_producto BIGINT NOT NULL REFERENCES inventario.producto (id_producto) ON DELETE CASCADE,
    descripcion VARCHAR(150) NOT NULL,
    cantidad DECIMAL(15, 2) NOT NULL,
    precio DECIMAL(15, 2) NOT NULL,
    orden INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX ix_presentacion_producto_producto
    ON inventario.presentacion_producto (id_producto);

CREATE UNIQUE INDEX ux_presentacion_producto_descripcion
    ON inventario.presentacion_producto (id_producto, descripcion);
