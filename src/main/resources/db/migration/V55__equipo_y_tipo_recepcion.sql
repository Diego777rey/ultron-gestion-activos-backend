-- =========================================================
-- V55: Equipos de clientes y tipo de recepción de la orden
-- =========================================================
-- Un equipo (ECU, tablero, BCM, llave, etc.) pertenece a un cliente y
-- puede estar montado en uno de sus vehículos, o no.
-- La orden de trabajo recepciona un VEHICULO o un EQUIPO.

CREATE TABLE IF NOT EXISTS patrimonio.equipo (
    id_equipo       BIGSERIAL PRIMARY KEY,
    id_cliente      BIGINT NOT NULL REFERENCES personas.cliente (id_cliente),
    id_vehiculo     BIGINT REFERENCES patrimonio.vehiculo (id_vehiculo) ON DELETE SET NULL,
    tipo_equipo     VARCHAR(60) NOT NULL,
    marca           VARCHAR(80),
    modelo          VARCHAR(80),
    numero_serie    VARCHAR(100),
    descripcion     VARCHAR(255),
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_registro  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_equipo_id_cliente ON patrimonio.equipo (id_cliente);
CREATE INDEX IF NOT EXISTS idx_equipo_id_vehiculo ON patrimonio.equipo (id_vehiculo) WHERE id_vehiculo IS NOT NULL;

COMMENT ON TABLE patrimonio.equipo IS 'Equipos o módulos electrónicos de clientes que ingresan al taller';
COMMENT ON COLUMN patrimonio.equipo.id_vehiculo IS 'Vehículo del cliente donde está montado el equipo. NULL si llega suelto';

ALTER TABLE taller.orden_trabajo
    ADD COLUMN IF NOT EXISTS tipo_recepcion VARCHAR(20) NOT NULL DEFAULT 'VEHICULO',
    ADD COLUMN IF NOT EXISTS id_equipo BIGINT REFERENCES patrimonio.equipo (id_equipo);

ALTER TABLE taller.orden_trabajo
    ADD CONSTRAINT ck_orden_trabajo_tipo_recepcion CHECK (tipo_recepcion IN ('VEHICULO', 'EQUIPO'));

CREATE INDEX IF NOT EXISTS idx_ot_equipo ON taller.orden_trabajo (id_equipo) WHERE id_equipo IS NOT NULL;

COMMENT ON COLUMN taller.orden_trabajo.tipo_recepcion IS 'VEHICULO: se recepciona el vehículo. EQUIPO: se recepciona un equipo, con o sin vehículo';

-- Permisos del módulo de equipos: los reciben los roles que ya gestionan vehículos.
INSERT INTO personas.permiso (modulo, accion, descripcion)
SELECT 'EQUIPOS', v.accion, v.descripcion
FROM (VALUES
    ('VER', 'Ver equipos'),
    ('CREAR', 'Crear equipos'),
    ('EDITAR', 'Editar equipos'),
    ('ELIMINAR', 'Eliminar equipos')
) AS v (accion, descripcion)
WHERE NOT EXISTS (
    SELECT 1 FROM personas.permiso p WHERE p.modulo = 'EQUIPOS' AND p.accion = v.accion
);

INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT DISTINCT rp.role_id, pe.id
FROM personas.role_permiso rp
JOIN personas.permiso pv ON pv.id = rp.permiso_id AND pv.modulo = 'VEHICULOS'
JOIN personas.permiso pe ON pe.modulo = 'EQUIPOS' AND pe.accion = pv.accion
WHERE NOT EXISTS (
    SELECT 1 FROM personas.role_permiso x WHERE x.role_id = rp.role_id AND x.permiso_id = pe.id
);
