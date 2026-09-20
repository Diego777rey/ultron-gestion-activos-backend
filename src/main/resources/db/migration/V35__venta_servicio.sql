-- Cobro de servicios desde el POS (sin stock).

ALTER TABLE financiero.detalle_venta
    ADD COLUMN id_servicio BIGINT REFERENCES inventario.servicio (id_servicio);

CREATE INDEX idx_detalle_venta_servicio
    ON financiero.detalle_venta (id_servicio)
    WHERE id_servicio IS NOT NULL;
