#!/usr/bin/env bash
# Segunda instancia de producción (cliente Campuzano).
# La instancia actual sigue en /opt/ultron, puerto 8081, base taller.
# Esta queda en /opt/ultron-campuzano, puerto 8082, base campuzano.
set -euo pipefail

MODE="${1:-install}"

if [[ "${EUID}" -ne 0 ]]; then
  if ! sudo -n true 2>/dev/null; then
    echo "Se necesita root o sudo sin contraseña para crear la base y el servicio."
    exit 1
  fi
  exec sudo -n bash "$0" "$@"
fi

PG_PORT=5551
DB_NAME=campuzano
DB_USER=ultron
DB_PASS=ultron
APP_PORT=8082
INSTALL_DIR=/opt/ultron-campuzano
SERVICE_NAME=ultron-campuzano
UPLOAD_DIR=/var/lib/ultron/uploads-campuzano
SOURCE_JAR=/opt/ultron/ultron.jar
# Hash BCrypt (strength 10) de la clave inicial. El login normaliza el usuario a mayúsculas.
PASSWORD_HASH='$2a$10$0.WO4m0crILk1EBMRHNGR.tOSOB.I3jkTjJuAGvFiVJ7PH.d2avtq'

psql_postgres() {
  sudo -u postgres psql -p "${PG_PORT}" -v ON_ERROR_STOP=1 "$@"
}

ensure_database() {
  echo "Preparando base '${DB_NAME}' en PostgreSQL (puerto ${PG_PORT})..."

  psql_postgres <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} WITH LOGIN PASSWORD '${DB_PASS}';
  END IF;
END
\$\$;
SQL

  if psql_postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" | grep -q 1; then
    echo "La base '${DB_NAME}' ya existe."
  else
    echo "Creando base '${DB_NAME}'..."
    psql_postgres -c "CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};"
  fi

  psql_postgres -d "${DB_NAME}" <<SQL
GRANT ALL ON SCHEMA public TO ${DB_USER};
ALTER SCHEMA public OWNER TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ${DB_USER};
SQL
}

install_instance() {
  if [[ ! -f "${SOURCE_JAR}" ]]; then
    echo "No está el JAR de la instancia actual: ${SOURCE_JAR}"
    exit 1
  fi

  local java_bin service_user service_group
  java_bin="$(systemctl cat ultron.service | awk '/^ExecStart=/{line=$0} END{print line}' | sed 's/^ExecStart=//' | awk '{print $1}')"
  service_user="$(systemctl cat ultron.service | awk -F= '/^User=/{user=$2} END{print user}')"
  service_group="$(systemctl cat ultron.service | awk -F= '/^Group=/{group=$2} END{print group}')"
  service_user="${service_user:-ultron-server}"
  service_group="${service_group:-${service_user}}"

  if [[ -z "${java_bin}" || ! -x "${java_bin}" ]]; then
    echo "No se pudo detectar el Java del servicio ultron."
    exit 1
  fi

  if ! id "${service_user}" >/dev/null 2>&1; then
    echo "No existe el usuario de servicio ${service_user}."
    exit 1
  fi

  echo "Java: ${java_bin}"
  echo "Usuario de servicio: ${service_user}"

  mkdir -p "${INSTALL_DIR}/logs" "${UPLOAD_DIR}"
  cp "${SOURCE_JAR}" "${INSTALL_DIR}/ultron.jar"
  chown -R "${service_user}:${service_group}" "${INSTALL_DIR}" "${UPLOAD_DIR}"
  chmod 755 "${INSTALL_DIR}" "${UPLOAD_DIR}"
  chmod 644 "${INSTALL_DIR}/ultron.jar"

  umask 077
  cat > "${INSTALL_DIR}/ultron.env" <<EOF
SERVER_PORT=${APP_PORT}
SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:${PG_PORT}/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASS}
ULTRON_UPLOAD_DIR=${UPLOAD_DIR}
EOF
  chown root:root "${INSTALL_DIR}/ultron.env"
  chmod 600 "${INSTALL_DIR}/ultron.env"

  cat > "/etc/systemd/system/${SERVICE_NAME}.service" <<EOF
[Unit]
Description=Ultron Gestion de Activos (Campuzano)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=${service_user}
Group=${service_group}
WorkingDirectory=${INSTALL_DIR}
EnvironmentFile=${INSTALL_DIR}/ultron.env
ExecStart=${java_bin} -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -Dlogging.file.name=logs/ultron.log -jar ${INSTALL_DIR}/ultron.jar
Restart=always
RestartSec=10
TimeoutStopSec=60
SuccessExitStatus=143
KillSignal=SIGTERM
LimitNOFILE=65535
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${SERVICE_NAME}

[Install]
WantedBy=multi-user.target
EOF

  if command -v ufw >/dev/null 2>&1 && ufw status | grep -q 'Status: active'; then
    ufw allow "${APP_PORT}/tcp"
  fi

  systemctl daemon-reload
  systemctl enable "${SERVICE_NAME}.service"
  systemctl restart "${SERVICE_NAME}.service"

  echo "Instancia ${SERVICE_NAME} instalada en el puerto ${APP_PORT}, base ${DB_NAME}."
}

seed_user() {
  echo "Asegurando el usuario inicial de '${DB_NAME}'..."

  psql_postgres -d "${DB_NAME}" -v hash="${PASSWORD_HASH}" <<'SQL'
INSERT INTO personas.usuario (username, password, activo)
SELECT 'PAULINHO', :'hash', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM personas.usuario WHERE username = 'PAULINHO'
);

INSERT INTO personas.usuario_role (usuario_id, role_id)
SELECT u.id, r.id
FROM personas.usuario u
CROSS JOIN personas.role r
WHERE u.username = 'PAULINHO'
ON CONFLICT DO NOTHING;

DO $$
DECLARE
    total integer;
BEGIN
    SELECT count(*) INTO total
    FROM personas.usuario_role ur
    JOIN personas.usuario u ON u.id = ur.usuario_id
    WHERE u.username = 'PAULINHO';

    IF total = 0 THEN
        RAISE EXCEPTION 'PAULINHO quedó sin roles';
    END IF;
END
$$;

SELECT u.username, r.descripcion
FROM personas.usuario u
JOIN personas.usuario_role ur ON ur.usuario_id = u.id
JOIN personas.role r ON r.id = ur.role_id
WHERE u.username = 'PAULINHO'
ORDER BY r.id;
SQL
}

case "${MODE}" in
  install)
    ensure_database
    install_instance
    ;;
  seed)
    seed_user
    ;;
  *)
    echo "Uso: $0 install|seed"
    exit 1
    ;;
esac
