-- =============================================================
-- V64: Sector donde ingresa al stock la mercadería de una compra
-- =============================================================

ALTER TABLE compras.compra
    ADD COLUMN id_sector BIGINT REFERENCES sectores.sector(id_sector);

CREATE INDEX idx_compra_sector ON compras.compra(id_sector);

COMMENT ON COLUMN compras.compra.id_sector IS 'Sector (lugar físico) donde ingresa la mercadería al stock cuando la compra está RECIBIDA';
