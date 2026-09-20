-- Un maletín no puede estar en dos sesiones abiertas a la vez.
-- Queda libre recién cuando se cierra la caja.

CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_maletin_abierta
    ON financiero.sesion_caja (id_maletin)
    WHERE estado = 'ABIERTA';
