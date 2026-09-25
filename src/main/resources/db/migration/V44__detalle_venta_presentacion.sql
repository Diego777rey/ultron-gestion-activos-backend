-- La venta guarda qué presentación se cobró (unidad, pack, caja).
-- El precio y las unidades de stock salen de esa presentación.

ALTER TABLE financiero.detalle_venta
    ADD COLUMN id_presentacion_producto BIGINT NULL
        REFERENCES inventario.presentacion_producto (id_presentacion_producto);

CREATE INDEX idx_detalle_venta_presentacion
    ON financiero.detalle_venta (id_presentacion_producto);
