-- Relación: un cliente puede tener muchos vehículos, cada vehículo pertenece a un cliente
ALTER TABLE patrimonio.vehiculo
    ADD COLUMN id_cliente BIGINT NOT NULL REFERENCES personas.cliente (id_cliente);

CREATE INDEX idx_vehiculo_id_cliente ON patrimonio.vehiculo (id_cliente);

CREATE UNIQUE INDEX idx_vehiculo_chapa_unica
    ON patrimonio.vehiculo (UPPER(chapa))
    WHERE chapa IS NOT NULL AND chapa <> '';
