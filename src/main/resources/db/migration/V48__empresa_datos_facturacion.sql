ALTER TABLE personas.empresa
    ADD COLUMN IF NOT EXISTS telefono            VARCHAR(50),
    ADD COLUMN IF NOT EXISTS email               VARCHAR(255),
    ADD COLUMN IF NOT EXISTS actividad_economica VARCHAR(255),
    ADD COLUMN IF NOT EXISTS logo                VARCHAR(500),
    ADD COLUMN IF NOT EXISTS activa              BOOLEAN DEFAULT TRUE;

COMMENT ON COLUMN personas.empresa.telefono IS 'Teléfono de contacto que se imprime en la factura';
COMMENT ON COLUMN personas.empresa.email IS 'Email de contacto que se imprime en la factura';
COMMENT ON COLUMN personas.empresa.actividad_economica IS 'Actividad económica declarada de la empresa';
COMMENT ON COLUMN personas.empresa.logo IS 'Ruta relativa del logo dentro de la carpeta de archivos del servidor';
COMMENT ON COLUMN personas.empresa.activa IS 'Indica si la empresa está activa';

UPDATE personas.empresa SET activa = TRUE WHERE activa IS NULL;
