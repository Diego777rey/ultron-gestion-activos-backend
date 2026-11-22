-- Terminales POS: cada PC Electron se registra sola con su MAC.
-- El admin asigna una caja a la terminal de forma remota.

CREATE TABLE IF NOT EXISTS financiero.terminal (
    id_terminal      BIGSERIAL PRIMARY KEY,
    mac_address      VARCHAR(17) NOT NULL,
    hostname         VARCHAR(255),
    nombre           VARCHAR(255),
    plataforma       VARCHAR(64),
    ultima_conexion  TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_registro   TIMESTAMP NOT NULL DEFAULT NOW(),
    activa           BOOLEAN NOT NULL DEFAULT TRUE,
    id_caja          BIGINT UNIQUE REFERENCES financiero.caja (id_caja)
);

COMMENT ON TABLE financiero.terminal IS
    'PCs donde corre la app de escritorio. Se registran al abrir la app.';
COMMENT ON COLUMN financiero.terminal.mac_address IS
    'MAC normalizada AA:BB:CC:DD:EE:FF.';
COMMENT ON COLUMN financiero.terminal.id_caja IS
    'Caja habilitada en esta PC (asignación remota). NULL = sin caja.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_terminal_mac_address
    ON financiero.terminal (mac_address);

CREATE INDEX IF NOT EXISTS idx_terminal_id_caja
    ON financiero.terminal (id_caja)
    WHERE id_caja IS NOT NULL;

-- Permisos (Admin los recibe automáticamente abajo).
INSERT INTO personas.permiso (modulo, accion, descripcion)
SELECT v.modulo, v.accion, v.descripcion
FROM (VALUES
    ('TERMINALES', 'VER', 'Ver terminales POS'),
    ('TERMINALES', 'EDITAR', 'Asignar o desasignar cajas a terminales')
) AS v (modulo, accion, descripcion)
WHERE NOT EXISTS (
    SELECT 1 FROM personas.permiso p WHERE p.modulo = v.modulo AND p.accion = v.accion
);

INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT r.id, p.id
FROM personas.role r
JOIN personas.permiso p ON p.modulo = 'TERMINALES'
WHERE r.descripcion = 'Admin'
  AND NOT EXISTS (
      SELECT 1 FROM personas.role_permiso x
      WHERE x.role_id = r.id AND x.permiso_id = p.id
  );
