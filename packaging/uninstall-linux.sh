#!/usr/bin/env bash
set -euo pipefail

INSTALL_DIR="${ULTRON_INSTALL_DIR:-/opt/ultron}"
SERVICE_USER="${ULTRON_SERVICE_USER:-ultron-server}"
SERVICE_NAME="ultron"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Ejecutar como root: sudo $0"
  exit 1
fi

if systemctl list-unit-files "${SERVICE_NAME}.service" >/dev/null 2>&1; then
  systemctl stop "${SERVICE_NAME}.service" >/dev/null 2>&1 || true
  systemctl disable "${SERVICE_NAME}.service" >/dev/null 2>&1 || true
fi

rm -f "/etc/systemd/system/${SERVICE_NAME}.service"
systemctl daemon-reload

if [[ -d "${INSTALL_DIR}" ]]; then
  rm -rf "${INSTALL_DIR}"
  echo "Eliminado ${INSTALL_DIR}"
fi

if id "${SERVICE_USER}" >/dev/null 2>&1; then
  userdel "${SERVICE_USER}" >/dev/null 2>&1 || true
fi

echo "Servicio Ultron desinstalado."
