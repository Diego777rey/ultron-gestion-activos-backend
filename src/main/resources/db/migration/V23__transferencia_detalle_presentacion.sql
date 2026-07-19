-- V23: Agregar referencia a presentacion_producto en transferencia_detalle
-- para soportar selección de presentación al transferir productos.

ALTER TABLE operaciones.transferencia_detalle
    ADD COLUMN id_presentacion_producto BIGINT NULL;

ALTER TABLE operaciones.transferencia_detalle
    ADD CONSTRAINT fk_transf_detalle_presentacion
        FOREIGN KEY (id_presentacion_producto)
            REFERENCES inventario.presentacion_producto (id_presentacion_producto);
