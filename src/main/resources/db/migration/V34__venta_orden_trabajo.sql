-- Cobro de órdenes de trabajo finalizadas desde el POS.
-- El detalle de venta puede representar una OT (sin producto ni descuento de stock).

ALTER TABLE financiero.detalle_venta
    ALTER COLUMN id_producto DROP NOT NULL;

ALTER TABLE financiero.detalle_venta
    ADD COLUMN descripcion VARCHAR(255),
    ADD COLUMN id_orden_trabajo BIGINT REFERENCES taller.orden_trabajo (id_orden_trabajo);

CREATE INDEX idx_detalle_venta_orden_trabajo
    ON financiero.detalle_venta (id_orden_trabajo)
    WHERE id_orden_trabajo IS NOT NULL;
