-- Vincula una caja a una terminal (PC) por dirección MAC.
-- Si mac_address está definida, esa caja solo puede abrirse en esa PC
-- y esa PC no puede abrir otras cajas.

ALTER TABLE financiero.caja
    ADD COLUMN IF NOT EXISTS mac_address VARCHAR(17),
    ADD COLUMN IF NOT EXISTS nombre_terminal VARCHAR(255);

COMMENT ON COLUMN financiero.caja.mac_address IS
    'MAC de la terminal Electron autorizada (AA:BB:CC:DD:EE:FF). NULL = sin vínculo.';
COMMENT ON COLUMN financiero.caja.nombre_terminal IS
    'Etiqueta opcional de la PC (hostname u otro nombre legible).';

CREATE UNIQUE INDEX IF NOT EXISTS uq_caja_mac_address
    ON financiero.caja (mac_address)
    WHERE mac_address IS NOT NULL AND btrim(mac_address) <> '';
